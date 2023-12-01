document.getElementById('connectBtn').addEventListener('click', function () {
    let ws = new WebSocket('ws://' + window.location.host + '/ws');

    ws.onopen = function () {
        console.log('WebSocket connection established');
    };

    ws.onmessage = function (event) {
        console.log('Received:', event.data);
    };

    ws.onclose = function () {
        console.log('WebSocket connection closed');
    };
});
