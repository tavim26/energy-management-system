import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { customerSupportService } from '../services/customerSupportService';

function CustomerSupport() {
  const { user } = useAuth();

  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSendMessage = async (e) => {
    e.preventDefault();

    if (!inputMessage.trim()) return;

    // Adauga mesajul utilizatorului in chat
    const userMessage = {
      sender: 'user',
      text: inputMessage,
      timestamp: new Date().toLocaleTimeString(),
    };
    setMessages((prev) => [...prev, userMessage]);

    const messageToSend = inputMessage;
    setInputMessage('');
    setLoading(true);

    try {
      // Trimite catre backend
      const response = await customerSupportService.sendMessage(
        user.userId,
        messageToSend
      );

      // Adauga raspunsul botului in chat
      const botMessage = {
        sender: 'bot',
        text: response.response,
        source: response.source,
        timestamp: new Date().toLocaleTimeString(),
      };
      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {

      console.error('Error sending message:', error);
      const errorMessage = {
        sender: 'bot',
        text: 'Eroare la trimiterea mesajului. Incearca din nou.',
        timestamp: new Date().toLocaleTimeString(),
      };
      setMessages((prev) => [...prev, errorMessage]);
      
    } finally {
      setLoading(false);
    }
  };

  const handleClearChat = () => {
    setMessages([]);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-2xl font-bold text-gray-800">Customer Support Chat</h2>
        {messages.length > 0 && (
          <button
            onClick={handleClearChat}
            className="text-sm text-red-600 hover:text-red-800"
          >
            Clear Chat
          </button>
        )}
      </div>

      {/* Chat messages */}
      <div className="border border-gray-300 rounded-lg p-4 h-96 overflow-y-auto mb-4 bg-gray-50">
        {messages.length === 0 ? (
          <div className="text-center text-gray-500 mt-20">
            <p>Trimite un mesaj pentru a incepe conversatia!</p>
            <p className="text-sm mt-2">
              Exemple: "cum resetez parola?", "unde vad consumul?", "ce dispozitive am?"
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {messages.map((msg, index) => (
              <div
                key={index}
                className={`flex ${
                  msg.sender === 'user' ? 'justify-end' : 'justify-start'
                }`}
              >
                <div
                  className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                    msg.sender === 'user'
                      ? 'bg-blue-500 text-white'
                      : 'bg-gray-200 text-gray-800'
                  }`}
                >
                  <p className="text-sm">{msg.text}</p>
                  <p className="text-xs mt-1 opacity-75">{msg.timestamp}</p>
                  {msg.source && (
                    <p className="text-xs mt-1 opacity-75">
                      [{msg.source === 'RULE' ? 'Automat' : 'Forwarded'}]
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Input form */}
      <form onSubmit={handleSendMessage} className="flex gap-2">
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          placeholder="Scrie mesajul tau aici..."
          disabled={loading}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          type="submit"
          disabled={loading || !inputMessage.trim()}
          className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
        >
          {loading ? 'Trimitere...' : 'Trimite'}
        </button>
      </form>
    </div>
  );
}

export default CustomerSupport;