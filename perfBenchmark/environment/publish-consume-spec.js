[
{'name': '1p-1c_Simple',
 'type': 'simple',
 'interval': 10000,
 'params': [{'time-limit': 60, 'producer-count': 1, 'consumer-count': 1}],
 'uri': 'amqp://testc:testp@localhost:5672/%2f'},
{'name': '1p-5c_Simple',
 'type': 'simple',
 'params':[{'time-limit': 60, 'producer-count': 1, 'consumer-count': 5}],
 'uri': 'amqp://testc:testp@localhost:5672/%2f'},
{'name': '5p-1c_Simple',
 'type': 'simple',
 'params':[{'time-limit': 60, 'producer-count': 5, 'consumer-count': 1}],
 'uri': 'amqp://testc:testp@localhost:5672/%2f'}
]
