import React, { useEffect } from 'react';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import webSocketService from '../services/websocketService';

import { useAuth } from '../context/AuthContext';  //  Get userId

function OverconsumptionNotification() {

  const { user } = useAuth();  // Get current user

  useEffect(() => {
    if (!user || !user.userId) {
      console.log('User not logged in, skipping WebSocket connection');
      return;
    }

    //  Conectare cu userId
    webSocketService.connect(
      user.userId,  // Pass userId
      () => {
        console.log('WebSocket connected for user:', user.userId);
      },
      (error) => {
        console.error('WebSocket connection failed:', error);
      }
    );

    const unsubscribe = webSocketService.subscribeToNotifications(
      (notification) => {
        if (notification.type === 'OVERCONSUMPTION') {
          toast.error(
            `ALERTA: Device ${notification.deviceId} a depasit limita!\n` +
              `Consum: ${notification.consumption.toFixed(2)} kWh\n` +
              `Limita: ${notification.limit.toFixed(2)} kWh`,
            {
              position: 'top-right',
              autoClose: 8000,
              hideProgressBar: false,
              closeOnClick: true,
              pauseOnHover: true,
              draggable: true,
            }
          );
        }
      }
    );

    return () => {
      unsubscribe();
      webSocketService.disconnect();
    };
  }, [user]);

  return (
    <ToastContainer
      position="top-right"
      autoClose={8000}
      hideProgressBar={false}
      newestOnTop
      closeOnClick
      rtl={false}
      pauseOnFocusLoss
      draggable
      pauseOnHover
      theme="colored"
    />
  );
}

export default OverconsumptionNotification;