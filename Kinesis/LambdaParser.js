'use strict';
console.log('Loading function');

/* Stock Ticker format parser */
const parser = /^\{\"TICKER_SYMBOL\"\:\"[A-Z]+\"\,\"SECTOR\"\:"[A-Z]+\"\,\"CHANGE\"\:[-.0-9]+\,\"PRICE\"\:[-.0-9]+\}/;

exports.handler = (event, context, callback) => {
    console.log(event)
    let success = 0; // Number of valid entries found
    let failure = 0; // Number of invalid entries found
    let dropped = 0; // Number of dropped entries 

    /* Process the list of records and transform them */
    const output = event.records.map((record) => {

        const entry = (new Buffer(record.data, 'base64')).toString('utf8');
        let match = parser.exec(entry);
        if (match) {
            let parsed_match = JSON.parse(match); 
            var milliseconds = new Date().getTime();
            /* Add timestamp and convert to CSV */
            const result = `${milliseconds},${parsed_match.ipaddress},${parsed_match.cpuusage},${parsed_match.memusage},${parsed_match.RootDiskUsage},${parsed_match.time}`+"\n";
            const payload = (new Buffer(result, 'utf8')).toString('base64');
          
                /* Dropped event, notify and leave the record intact */
                //dropped++;
            
            
                /* Transformed event */
                success++;  
                return {
                    recordId: record.recordId,
                    result: 'Ok',
                    data: payload,
                };
        }
            
    });
    console.log(`Processing completed.  Successful records ${output.length}.`);
    callback(null, { records: output });
};