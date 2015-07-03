# Reporting Tools
## 1. Generate **measure.json**
Collect the measured values by usage of glances and the [HTML Performance tools](https://github.com/philippsied/amqp-stress-test/tree/master/perfBenchmark/runscenario).

Use the provided BASH script **transformToJson.sh** to merge the CSV file (given by glances) and the JSON file (given by HTML Performance tools) into **measure.json**

I.e. to create the visualisation for TCP dropping scenario run:

`bash transformToJson.sh "TCP dropping" tcpdrop.csv tcpdrop.json`

The string "TCP dropping" will appear in the headline of the HTML-file.

## 2. Look at your results and, if necessary, adjust the visualisation

Open the **results.html** file in your web browser and the measured values should be clearly visible.

If something looks clipped off or inaccurate scaled, use the div attributes of the div-classes named **chart** to adjust the charts.
