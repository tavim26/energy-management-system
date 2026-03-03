import React, { createContext, useState, useContext } from 'react';

//context = depozit de date global accesibil mereu
//AuthContext = pastreaza date globale despre user, ofer functii de login/logout

//initializare context
const AuthContext = createContext(null);

//AuthProvider = furnizorul contextului care da acces la date
export const AuthProvider = ({ children }) => {

  // init din localStorage (pentru persistenta)
  // date pentru user-ul logat
  const [user, setUser] = useState(() => {
    const storedUserId = localStorage.getItem('userId');
    const storedUsername = localStorage.getItem('username');
    const storedRole = localStorage.getItem('role');
    
    if (storedUserId && storedUsername && storedRole) {
      return {
        userId: parseInt(storedUserId),
        username: storedUsername,
        role: storedRole
      };
    }
    return null;
  });
  


  // token primit de la backend la login
  const [token, setToken] = useState(localStorage.getItem('token'));



  // LOGIN
  const login = (userData) => {
    // save localStorage
    localStorage.setItem('token', userData.token);
    localStorage.setItem('userId', userData.userId);
    localStorage.setItem('username', userData.username);
    localStorage.setItem('role', userData.role);
    
    // update state React
    setToken(userData.token);
    setUser({
      userId: userData.userId,
      username: userData.username,
      role: userData.role
    });
  };



  // LOGOUT
  const logout = () => {
    // remove din localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    
    // reset state
    setToken(null);
    setUser(null);
  };

  // Helper functions (pt verificari rapide)
  const isAdmin = () => user?.role === 'ADMIN';
  const isClient = () => user?.role === 'CLIENT';
  const isAuthenticated = !!token;

  // value e obiect cu toate datele si functiile expuse in Context
  const value = {
    user,
    token,
    login,
    logout,
    isAuthenticated,
    isAdmin,
    isClient
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// custom hook pentru a accesa contextul usor
export const useAuth = () => {

  const context = useContext(AuthContext);
  
  //verif daca useAuth e folosit in interiorul AuthProvider
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  
  return context;
};