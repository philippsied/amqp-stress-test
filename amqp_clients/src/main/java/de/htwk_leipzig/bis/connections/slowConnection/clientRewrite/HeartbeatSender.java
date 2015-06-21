package de.htwk_leipzig.bis.connections.slowConnection.clientRewrite;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.Frame;
import com.rabbitmq.client.impl.FrameHandler;

/**
 * Manages heartbeat sending for a {@link AMQConnection}.
 * <p/>
 * Heartbeats are sent in a dedicated thread that is separate from the main loop
 * thread used for the connection.
 */
public final class HeartbeatSender {

	private final Object monitor = new Object();

	private final FrameHandler frameHandler;
	private final ThreadFactory threadFactory;

	private ScheduledExecutorService executor;

	private ScheduledFuture<?> future;

	private boolean shutdown = false;

	private volatile long lastActivityTime;

	public HeartbeatSender(FrameHandler frameHandler, ThreadFactory threadFactory) {
		this.frameHandler = frameHandler;
		this.threadFactory = threadFactory;
	}

	public void signalActivity() {
		this.lastActivityTime = System.nanoTime();
	}

	/**
	 * Sets the heartbeat in seconds.
	 */
	public void setHeartbeat(int heartbeatSeconds) {
		synchronized (this.monitor) {
			if (this.shutdown) {
				return;
			}

			// cancel any existing heartbeat task
			if (this.future != null) {
				this.future.cancel(true);
				this.future = null;
			}

			if (heartbeatSeconds > 0) {
				// wake every heartbeatSeconds / 2 to avoid the worst case
				// where the last activity comes just after the last heartbeat
				long interval = SECONDS.toNanos(heartbeatSeconds) / 2;
				ScheduledExecutorService executor = createExecutorIfNecessary();
				Runnable task = new HeartbeatRunnable(interval);
				this.future = executor.scheduleAtFixedRate(task, interval, interval, TimeUnit.NANOSECONDS);
			}
		}
	}

	private ScheduledExecutorService createExecutorIfNecessary() {
		synchronized (this.monitor) {
			if (this.executor == null) {
				this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
			}
			return this.executor;
		}
	}

	/**
	 * Shutdown the heartbeat process, if any.
	 */
	public void shutdown() {
		ExecutorService executorToShutdown = null;
		synchronized (this.monitor) {
			if (this.future != null) {
				this.future.cancel(true);
				this.future = null;
			}

			if (this.executor != null) {
				// to be safe, we shouldn't call shutdown holding the
				// monitor.
				executorToShutdown = this.executor;

				this.shutdown = true;
				this.executor = null;
			}
		}
		if (executorToShutdown != null) {
			executorToShutdown.shutdown();
		}
	}

	private final class HeartbeatRunnable implements Runnable {

		private final long heartbeatNanos;

		private HeartbeatRunnable(long heartbeatNanos) {
			this.heartbeatNanos = heartbeatNanos;
		}

		public void run() {
			try {
				long now = System.nanoTime();

				if (now > (lastActivityTime + this.heartbeatNanos)) {
					frameHandler.writeFrame(new Frame(AMQP.FRAME_HEARTBEAT, 0));
					frameHandler.flush();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
