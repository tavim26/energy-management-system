import axios from 'axios';

//toate request-urile trec prin gateway
const API_BASE_URL = 'http://localhost:8080';   

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'  //toate request-urile trimit json
  }
});

// interceptor - logica ce se executa inainte de fiecare request
// adauga jwt token in header la fiecare request 
apiClient.interceptors.request.use((config) => {
  
  const token = localStorage.getItem('token');  //preia token din localStorage
  
  if (token) 
  {
    config.headers.Authorization = `Bearer ${token}`; //header authorization
  }
  return config;
});


export default apiClient;