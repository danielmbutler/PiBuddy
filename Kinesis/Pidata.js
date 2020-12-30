const AWS = require('aws-sdk');


    var kinesis = new AWS.Kinesis({
        apiVersion: '2013-12-02',
        region: 'us-east-1'
    });


    var recordData = [];

    setInterval(function () {
        
        

    //dummy data

    // var CPUusage = "20%"
    // var Memusage = "40%"
    // var Diskusage ="90%"

    // get pi data    
            
    var execSync        = require('child_process').execSync;
    var CPUCommand      = 'mpstat | grep -A 5 "%idle" | tail -n 1 | awk -F " " \'{print 100 -  $ 12}\'a';
    var MemCommand      = 'free | grep Mem | awk \'{print $3/$2 * 100.0}\''
    var DiskCommand     = 'df -hl | grep \'root\' | awk \'BEGIN{print ""} {percent+=$5;} END{print percent}\' | column -t'
   
    
    var options = {
      encoding: 'utf8'
    };
    

    var CPUusage    = (execSync(CPUCommand, options))
    var Memusage    = (execSync(MemCommand, options))
    var Diskusage   = (execSync(DiskCommand, options))



    // Create the Amazon Kinesis record
    var record = {
        Data: JSON.stringify({
            ipaddress       : "192.168.1.81",
            cpuusage        : CPUusage.replace("\n",""),
            memusage        : Memusage.replace("\n",""),
            RootDiskUsage   : Diskusage.replace("\n",""),
            time: new Date()
        }),
    }

    recordData.push(record);



    console.log(recordData)
       
    }, 300000); //run every 5 minute
    

    

    //upload data to Amazon Kinesis every second if data exists
    setInterval(function() {
        if (!recordData.length) {
            return;
        }
        // upload data to Amazon Kinesis
        // kinesis.putRecords({
        //     Records: recordData,
        //     StreamName: "Pidata"
        // }, function(err, data) {
        //     if (err) {
        //         console.error(err);
        //     } else {
        //         console.log(data)
        //     }
        // });

        // upload to firehose

        //function streamKinesis(req, res){
            console.log(recordData)
            var params = {
                Record: {Data: JSON.stringify(recordData)},
                DeliveryStreamName: "Pidata"
            };
            var firehouse = new AWS.Firehose({region:'us-east-1'});
            firehouse.putRecord(params, function (err, data) {
                if (err) {
                    console.error("couldn't stream", err.stack);
                }
                else {
                    console.log("INFO - successfully send stream");
                    console.log(data)
                }
            });
            
            //streamKinesis()
        // clear record data
        recordData = [];
    }, 600000); //send every 10 minutes and clear object
