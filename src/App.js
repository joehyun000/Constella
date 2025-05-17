import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import GlobeViewer from "./pages/GlobeViewer";

function App() {
  // �α��� ���� (���÷� App���� ����)
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  return (
    <Router>
      <Routes>
        {/* �α��� �� ������ Home �����ְ�, �α����ϸ� GlobeViewer �����ֱ� */}
        <Route 
          path="/" 
          element={
            isLoggedIn ? <Navigate to="/globe" /> : <Home setIsLoggedIn={setIsLoggedIn} />
          } 
        />
        {/* GlobeViewer ��δ� �α��� ���ο� ���� ���� ���� ���� */}
        <Route 
          path="/globe" 
          element={isLoggedIn ? <GlobeViewer /> : <Navigate to="/" />} 
        />
      </Routes>
    </Router>
  );
}

export default App;
