
function render_graphs(results) {
    $('.chart, .small-chart').map(function() {
        plot($(this), results);
    });
    $('.summary').map(function() {
        summarise($(this), results);
    });
}

function summarise(div, results) {
    var scenario = div.attr('data-scenario');
    var mode     = div.attr('data-mode');
    var dataYfirst = div.attr('data-y1');
    var dataYsecond = div.attr('data-y2');
    var unitDataYAxis = div.attr('data-y-axis');
    var data     = results[scenario];

    var rate;
    if (mode == 'two') {
        rate = Math.round(data[dataYsecond]);
    }
    else if (mode == 'one') {
        rate = Math.round(data[dataYfirst]);
    }
    else {
        rate = Math.round((data[dataYsecond] + data[dataYfirst]) / 2);
    }

    div.append('<strong>' + rate + '</strong>' + unitDataYAxis);
}

function plot(div, results) {
    var file = div.attr('data-file');

    if (file == undefined) {
        plot0(div, results);
    }
    else {
        $.ajax({
            url: file,
            success: function(data) {
                plot0(div, JSON.parse(data));
            },
            fail: function() { alert('error loading ' + file); }
        });
    }
}

function plot0(div, results) {
    var type     = div.attr('data-type');
    var scenario = div.attr('data-scenario');

    if (type == 'time') {
        var data = results[scenario];
        plot_time(div, data);
    }
    else {
        var dimensions       = results[scenario]['dimensions'];
        var dimension_values = results[scenario]['dimension-values'];
        var data             = results[scenario]['data'];

        if (type == 'series') {
            plot_series(div, dimensions, dimension_values, data);
        }
        else if (type == 'x-y') {
            plot_x_y(div, dimensions, dimension_values, data);
        }
        else if (type == 'r-l') {
            plot_r_l(div, dimensions, dimension_values, data);
        }
    }
}

function plot_time(div, data) {
    var show_y3 = div.attr('data-show-y3') == 'true';
    var dataYfirst = div.attr('data-y1');
    var dataYsecond = div.attr('data-y2');
    var dataYthird = div.attr('data-y3');
    var labelYfirst = attr_or_default(div, 'data-y1-label', dataYfirst);
    var labelYsecond = attr_or_default(div, 'data-y2-label', dataYsecond);
    var labelYthird = attr_or_default(div, 'data-y3-label', dataYthird);
    var chart_data = [];
  
    var keys = show_y3
       ? [dataYfirst, dataYsecond, dataYthird]
        : [dataYfirst, dataYsecond];
    var labels = show_y3
       ? [labelYfirst, labelYsecond, labelYthird]
        : [labelYfirst, labelYsecond];
    $.each(keys, function(i, plot_key) {
        var d = [];
        $.each(data['samples'], function(j, sample) {
            d.push([sample['elapsed'] / 1000, sample[plot_key]]);
        });
        var yaxis = (plot_key.indexOf(dataYthird) == -1 ? 1 : 2);
        chart_data.push({label: labels[i], data: d, yaxis: yaxis});
    });

    plot_data(div, chart_data, {yaxes: axes_rate_and_cpu_load});
}

function plot_series(div, dimensions, dimension_values, data) {
    var x_key         = div.attr('data-x-key');
    var series_key    = div.attr('data-series-key');
    var series_first  = dimensions[0] == series_key;
    var series_values = dimension_values[series_key];
    var x_values      = dimension_values[x_key];
    var dataYfirst = div.attr('data-y1');
    var dataYsecond = div.attr('data-y2');
    var plot_key      = attr_or_default(div, 'plot-key', dataYsecond);

    var chart_data = [];
    $.each(series_values, function(i, s_val) {
        var d = [];
        $.each(x_values, function(j, x_val) {
            var val = series_first ? data[s_val][x_val] :
                                     data[x_val][s_val];
            d.push([x_val, val[plot_key]]);
        });
        chart_data.push({label: series_key + ' = ' + s_val, data: d});
    });

    plot_data(div, chart_data);
}

function plot_x_y(div, dimensions, dimension_values, data) {
    var x_key = div.attr('data-x-key');
    var x_values = dimension_values[x_key];
    var dataYfirst = div.attr('data-y1');
    var dataYsecond = div.attr('data-y2');
    var plot_keys = attr_or_default(div, 'plot-keys', dataYsecond).split(' ');
    var chart_data = [];
    var extra = {};
    $.each(plot_keys, function(i, plot_key) {
        var d = [];
        $.each(x_values, function(j, x_val) {
            d.push([x_val, data[x_val][plot_key]]);
        });
        var yaxis = 1;
        if (plot_key.indexOf('bytes') != -1) {
            yaxis = 2;
            extra = {yaxes: axes_rate_and_bytes};
        }
        chart_data.push({label: plot_key, data: d, yaxis: yaxis});
    });
    plot_data(div, chart_data, extra);
}

function plot_r_l(div, dimensions, dimension_values, data) {
    var x_values = dimension_values['producerRateLimit'];
    var dataYfirst = div.attr('data-y1');
    var dataYsecond = div.attr('data-y2');
    var chart_data = [];
    var d = [];
    $.each(x_values, function(i, x_val) {
        d.push([x_val, data[x_val][dataYsecond]]);
    });
    chart_data.push({label: 'rate achieved', data: d, yaxis: 1});

    d = [];
    $.each(x_values, function(i, x_val) {
        d.push([x_val, data[x_val]['cpu-load']]);
    });
    chart_data.push({label: 'cpu-load (us)', data: d, yaxis: 2});

    plot_data(div, chart_data, {yaxes: axes_rate_and_cpu_load});
}

function plot_data(div, chart_data, extra) {
    var legend     = attr_or_default(div, 'data-legend', 'se');
    var x_axis_log = attr_or_default(div, 'x-axis-log', 'false') == 'true';
    var cssClass   = div.attr('class');

    var chrome = {
        series: { lines: { show: true } },
        grid:   { borderWidth: 2, borderColor: "#aaa" },
        xaxis:  { tickColor: "#fff" },
        yaxis:  { tickColor: "#eee" },
        legend: { position: legend, backgroundOpacity: 0.5 }
    };

    if (div.attr('class') == 'small-chart') {
        chrome['legend'] = { show: false };
    }

    if (extra != undefined) {
        for (var k in extra) {
            chrome[k] = extra[k];
        }
    }

    if (x_axis_log) {
        chrome['xaxis'] = log_x_axis;
    }

    var cell = div.wrap('<td />').parent();;
    var row = cell.wrap('<tr/>').parent();
    row.wrap('<table class="' + cssClass + '-wrapper"/>');

    cell.before('<td class="yaxis">' + div.attr('data-y-axis') + '</td>');
    if (div.attr('data-y-axis2')) {
        cell.after('<td class="yaxis">' + div.attr('data-y-axis2') + '</td>');
    }
    row.after('<tr><td></td><td class="xaxis">' + div.attr('data-x-axis') +
              '</td><td></td></tr>');

    $.plot(div, chart_data, chrome);
}


function log_transform(v) {
    return Math.log(v);
}

function log_ticks(axis) {
    var val = axis.min;
    var res = [val];
    while (val < axis.max) {
        val *= 10;
        res.push(val);
    }
    return res;
}

function attr_or_default(div, key, def) {
    var res = div.attr(key);
    return res == undefined ? def : res;
}

var axes_rate_and_cpu_load = [{ min:0, position:"left" },
                              { min:0, position:"right" }];

var axes_rate_and_bytes = [{ min:0},
                           { min:0, position:"right" }];

var log_x_axis = {min:1,
                  transform: log_transform,
                  ticks:     log_ticks};
