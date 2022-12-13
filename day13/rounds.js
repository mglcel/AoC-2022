const nReadlines = require('n-readlines');

const comparePackets = (left, right) => {
    if (typeof(right) === 'undefined')
        return 1; 
    if (left.constructor == Array) {
        if (right.constructor == Array) {
            for (var test, i = 0; i < left.length; i++)
                if ((test = comparePackets(left[i], right[i])) != 0)
                    return test;
            return Math.sign(left.length - right.length);
        } else return comparePackets(left, [right]);
    } else if (right.constructor == Array)
        return comparePackets([left], right);
    return Math.sign(left - right);
}

const inputLines = new nReadlines('input.txt');

var index = 1, round1 = 0, packets = [];
while ((left = inputLines.next()) && (right = inputLines.next()) && (index++)) {
    left = JSON.parse(left), right = JSON.parse(right);
    if (comparePackets(left, right) < 0)
        round1 += index;
    packets.push(left, right);
    inputLines.next();
}
console.log("Round 1: " + round1);

var dividers = ["[[2]]", "[[6]]"];
dividers.map(p => packets.push(JSON.parse(p)));
var decoderKey = packets.sort((p1, p2) => comparePackets(p1, p2))
                        .map((p, i) => [p, i])
                        .filter((packet) => dividers.includes(JSON.stringify(packet[0])))
                        .map((p) => p[1] + 1)
                        .reduce((a, b) => a * b, 1);
console.log("Round 2: " + decoderKey);