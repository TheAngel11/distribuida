// Open WebSocket connection
let ws = new WebSocket('ws://' + window.location.host + '/ws');

ws.onopen = function () {
    document.getElementById('status').innerHTML = 'Connected';
    document.getElementById('status').style.color = 'green';
};

// Listen for messages
ws.onmessage = function (event) {
    let data = event.data.split(';');
    document.getElementById(data[0]).innerHTML = data[1];
};

ws.onclose = function () {
    document.getElementById('status').innerHTML = 'Disconnected';
    document.getElementById('status').style.color = 'red';
}