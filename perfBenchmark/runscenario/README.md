# Start and measure test scenario

Customize the parameter of the scenario, given by the file **publish-consume-spec.template**

Use the provided BASH script **startBenchmark.sh** to start the measure.

i.e. For connecting as user "user" with password "pass" on localhost at port 5672 to virtualhost "/" and write the results to "result.json"

`bash startBenchmark.sh "result.json" user:pass@localhost:5672/%2f`

