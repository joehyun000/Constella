import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import GlobeViewer from "./pages/GlobeViewer";

function App() {
  // 로그인 상태 (예시로 App에서 관리)
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  return (
    <Router>
      <Routes>
        {/* 로그인 안 했으면 Home 보여주고, 로그인하면 GlobeViewer 보여주기 */}
        <Route 
          path="/" 
          element={
            isLoggedIn ? <Navigate to="/globe" /> : <Home setIsLoggedIn={setIsLoggedIn} />
          } 
        />
        {/* GlobeViewer 경로는 로그인 여부에 따라 접근 제한 가능 */}
        <Route 
          path="/globe" 
          element={isLoggedIn ? <GlobeViewer /> : <Navigate to="/" />} 
        />
      </Routes>
    </Router>
  );
}

export default App;
