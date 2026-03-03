import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function ProtectedRoute({ children, allowedRoles }) {
  const { user, isAuthenticated } = useAuth();
  
  // verifica daca user e logat
  // daca nu, reddirect la login
  if (!isAuthenticated) 
    {
    return <Navigate to="/login" replace />;
  }
  
  // verifica daca user are rol permis (ADMIN sau CLIENT)
  // daca nu, reddirect la Unauthorized
  if (allowedRoles && !allowedRoles.includes(user?.role)) 
    {
    return <Navigate to="/unauthorized" replace />;
  }
  
  return children;
}

export default ProtectedRoute;