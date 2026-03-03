import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.subscribers = {
      notifications: [],
    };
  }

  connect(userId, onConnected, onError) {  // Accept userId as parameter
    
    const apiUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';
    
    console.log('Connecting to WebSocket at:', `${apiUrl}/ws`);
    
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(`${apiUrl}/ws`),
      
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: (frame) => {
        console.log('WebSocket connected successfully:', frame);
        this.connected = true;

        // Subscribe la topic specific user-ului
        const userTopic = `/topic/notifications/${userId}`;
        console.log('Subscribing to:', userTopic);
        
        this.stompClient.subscribe(userTopic, (message) => {
          const notification = JSON.parse(message.body);
          console.log('Notification received:', notification);

          this.subscribers.notifications.forEach((callback) => {
            callback(notification);
          });
        });

        if (onConnected) onConnected();
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.connected = false;
        if (onError) onError(frame);
      },

      onWebSocketError: (error) => {
        console.error('WebSocket error:', error);
        this.connected = false;
        if (onError) onError(error);
      },
    });

    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient && this.connected) {
      this.stompClient.deactivate();
      console.log('WebSocket disconnected');
      this.connected = false;
    }
  }

  subscribeToNotifications(callback) {
    this.subscribers.notifications.push(callback);

    return () => {
      this.subscribers.notifications = this.subscribers.notifications.filter(
        (cb) => cb !== callback
      );
    };
  }

  isConnected() {
    return this.connected;
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;