package web_server

import (
	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"net/http"
)

// wsUpgrader is used to upgrade a HTTP connection to a WebSocket connection.
var wsUpgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
}

// wsHandler handles WebSocket connections.
func wsHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := wsUpgrader.Upgrade(w, r, nil)
	if err != nil {
		http.Error(w, "Could not open WebSocket connection", http.StatusBadRequest)
		return
	}

	// Handle WebSocket connection
	go handleConnection(conn)
}

// handleConnection handles a WebSocket connection.
func handleConnection(conn *websocket.Conn) {
	defer conn.Close()

	for {
		// Read message from WebSocket connection
		_, msg, err := conn.ReadMessage()
		if err != nil {
			return
		}

		// Write message to WebSocket connection
		err = conn.WriteMessage(websocket.TextMessage, msg)
		if err != nil {
			return
		}
	}
}

// Start starts the web server.
func Start() {
	r := gin.Default()

	// Serve HTML and JavaScript files
	r.LoadHTMLFiles("web_server/page/index.html")
	r.Static("/js", "web_server/page")
	r.Static("/css", "web_server/page")
	r.GET("/", func(c *gin.Context) {
		c.HTML(http.StatusOK, "index.html", nil)
	})

	// WebSocket endpoint
	r.GET("/ws", func(c *gin.Context) {
		wsHandler(c.Writer, c.Request)
	})

	// Start server
	r.Run(":8080")
}
