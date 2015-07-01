/**
 * 
 */



/* 
 * 
 */
function render_graphs(results) {
    $("#headline").text($("#headline").text() +' "' + results['name'] + '"');
    
    $('.chart, .small-chart').map(function() {
        plot0($(this), results);
    });
    $('.summary').map(function() {
        summarise($(this), results);
    });
}




/* 
 * 
 */
function summarise(div, results) {
    var scenario = div.attr('data-scenario');
    var data = results[scenario];

    var dataYfirst = div.attr('data-y1');
    if( dataYfirst && (data[dataYfirst] !=  undefined ) ){
        div.append('&#216; ' + attr_or_default(div, 'data-y1-label', dataYfirst) + '<strong>' + Math.round(data[dataYfirst]) + div.attr('data-y-axis-unit') + '</strong><br>');
    }

    var dataYsecond = div.attr('data-y2');
    if( dataYsecond && (data[dataYsecond] != undefined ) ){
        div.append('&#216; ' + attr_or_default(div, 'data-y2-label', dataYsecond) + '<strong>' + Math.round(data[dataYsecond]) + div.attr('data-y-axis-unit') + '</strong><br>');
    }

    var dataYthird = div.attr('data-y3');
    if( (div.attr('data-show-y3') == 'true') && dataYthird && (data[dataYthird] !=  undefined ) ){
        div.append('&#216; ' + attr_or_default(div, 'data-y3-label', dataYthird) + '<strong>' + Math.round(data[dataYthird]) + div.attr('data-y-axis2-unit') + '</strong>');
    }
}


/* 
 * 
 */
function plot0(div, results) {
    var type     = div.attr('data-type');
    var scenario = div.attr('data-scenario');

    if (type == 'time') {
        var data = results[scenario];
        plot_time(div, data);
    }
}


/* 
 * 
 */
function plot_time(div, data) {
    var dataYfirst = div.attr('data-y1');
    var dataYsecond = div.attr('data-y2');
    var labelYfirst = attr_or_default(div, 'data-y1-label', dataYfirst);
    var labelYsecond = attr_or_default(div, 'data-y2-label', dataYsecond);
    var show_y3 = div.attr('data-show-y3') == 'true';
    if (show_y3) {
      var dataYthird = attr_or_default(div, 'data-y3','');
      var labelYthird = attr_or_default(div, 'data-y3-label', dataYthird);
    } 
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
    
    var prop_y_axes = [{ position:"left" },{ position:"right" }];
    if( div.attr('data-y-axis-minVal') ){
        prop_y_axes[0].min = div.attr('data-y-axis-minVal');
    }
    if( div.attr('data-y-axis-maxVal') ){
        prop_y_axes[0].max = div.attr('data-y-axis-maxVal');
    }
    if( div.attr('data-y-axis2-minVal') ){
        prop_y_axes[1].min = div.attr('data-y-axis2-minVal');
    }
    if( div.attr('data-y-axis2-maxVal') ){
        prop_y_axes[1].max = div.attr('data-y-axis2-maxVal');
    }

    plot_data(div, chart_data, {yaxes: prop_y_axes});
}


/* 
 * 
 */
function plot_data(div, chart_data, extra) {
    var legend     = attr_or_default(div, 'data-legend', 'se');
    var x_axis_log = attr_or_default(div, 'x-axis-log', 'false') == 'true';
    var cssClass   = div.attr('class');

    var chrome = {
        series: { lines: { show: true } },
        grid:   { borderWidth: 2, borderColor: "#aaa" , hoverable: true },
        xaxis:  { tickColor: "#fff", labelHeight: 30 },
        yaxis:  { tickColor: "#eee", labelWidth: 50 },
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
    if ( div.attr('data-show-y3') == 'true' && div.attr('data-y-axis2') ) {
        cell.after('<td class="yaxis">' + div.attr('data-y-axis2') + '</td>');
    }
    row.after('<tr><td></td><td class="xaxis">' + div.attr('data-x-axis') +
              '</td><td></td></tr>');
    registerToolTip(div);
    $.plot(div, chart_data, chrome);
}


/*
 *
 */
function registerToolTip(div){
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");

    div.bind("plothover", function (event, pos, item) {
        if (item) {
            var x = item.datapoint[0].toFixed(0);
            var y = item.datapoint[1].toFixed(0);
            $("#tooltip").html(item.series.label + " - Bei " + x + "s: " + y)
                .css({top: item.pageY+5, left: item.pageX+5})
                .fadeIn(200);
        } else {
            $("#tooltip").hide();
        }
   });
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

var log_x_axis = {min:1,
                  transform: log_transform,
                  ticks:     log_ticks};

                  
/*
 * @unused
 */
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
