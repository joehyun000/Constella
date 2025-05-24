import React, { useEffect, useRef, useState } from "react";
import * as d3 from "d3";
import { useNavigate } from "react-router-dom";
import worldMapData from "./custom.geo.json";

const STAR_RADIUS = 1.5;
const CONSTELLATION_LINE_DISTANCE = 70;

function createStarsFromGeo(width, height) {
  const projection = d3.geoMercator()
    .translate([width / 2, height / 2])
    .center([0, 15])
    .scale(width / 6);

  let stars = [];

  worldMapData.features.forEach((feature) => {
    const coordinates = feature.geometry.coordinates;

    if (feature.geometry.type === "Polygon") {
      coordinates[0].filter((_, i) => i % 10 === 0).forEach(([lon, lat]) => {
        const [x, y] = projection([lon, lat]);
        stars.push({
          x,
          y,
          radius: Math.random() * STAR_RADIUS + 0.5,
          twinkleSpeed: 0.002 + Math.random() * 0.004,
          opacity: Math.random() * 0.4 + 0.6,
        });
      });
    }

    if (feature.geometry.type === "MultiPolygon") {
      coordinates.forEach((polygon) => {
        polygon[0].filter((_, i) => i === 0).forEach(([lon, lat]) => {
          const [x, y] = projection([lon, lat]);
          stars.push({
            x,
            y,
            radius: Math.random() * STAR_RADIUS + 0.5,
            twinkleSpeed: 0.002 + Math.random() * 0.004,
            opacity: Math.random(),
          });
        });
      });
    }
  });

  return stars;
}

export default function Home({ setIsLoggedIn }) {
  const canvasRef = useRef(null);
  const [stars, setStars] = useState([]);
  const [mousePos, setMousePos] = useState({ x: -9999, y: -9999 });
  const [selectedStar, setSelectedStar] = useState(null);
  const [scrollY, setScrollY] = useState(0);
  const [showLoginBox, setShowLoginBox] = useState(false);
  const [showRegisterBox, setShowRegisterBox] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleResize = () => {
      const width = window.innerWidth;
      const height = window.innerHeight;
      setStars(createStarsFromGeo(width, height));
      if (canvasRef.current) {
        canvasRef.current.width = width;
        canvasRef.current.height = height;
      }
    };
    handleResize();
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    const onScroll = () => setScrollY(window.scrollY);
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    let animationFrameId;

    function animate() {
      const width = canvas.width;
      const height = canvas.height;
      ctx.clearRect(0, 0, width, height);

      ctx.strokeStyle = "rgba(255, 255, 255, 0.15)";
      ctx.lineWidth = 1;
      ctx.beginPath();
      for (let i = 0; i < stars.length; i++) {
        for (let j = i + 1; j < stars.length; j++) {
          const dx = stars[i].x - stars[j].x;
          const dy = stars[i].y - stars[j].y;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (
            dist < CONSTELLATION_LINE_DISTANCE &&
            stars[i].opacity > 0.7 &&
            stars[j].opacity > 0.7
          ) {
            ctx.moveTo(stars[i].x, stars[i].y);
            ctx.lineTo(stars[j].x, stars[j].y);
          }
        }
      }
      ctx.stroke();

      stars.forEach((star) => {
        const dx = star.x - mousePos.x;
        const dy = star.y - mousePos.y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < CONSTELLATION_LINE_DISTANCE) {
          ctx.beginPath();
          ctx.strokeStyle = `rgba(255, 255, 255, ${1 - dist / CONSTELLATION_LINE_DISTANCE})`;
          ctx.lineWidth = 1.5;
          ctx.shadowColor = "white";
          ctx.shadowBlur = 12;
          ctx.moveTo(star.x, star.y);
          ctx.lineTo(mousePos.x, mousePos.y);
          ctx.stroke();
        }
      });

      stars.forEach((star) => {
        star.opacity += star.twinkleSpeed;
        if (star.opacity >= 1 || star.opacity <= 0.6) {
          star.twinkleSpeed = -star.twinkleSpeed;
        }
        ctx.beginPath();
        ctx.fillStyle = `rgba(255, 255, 255, ${star.opacity})`;
        ctx.shadowColor = "white";
        ctx.shadowBlur = 15;
        ctx.arc(star.x, star.y, star.radius, 0, Math.PI * 2);
        ctx.fill();
      });

      animationFrameId = requestAnimationFrame(animate);
    }
    animate();
    return () => cancelAnimationFrame(animationFrameId);
  }, [stars, mousePos]);

  const handleCanvasClick = (e) => {
    const rect = canvasRef.current.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;
    const clickedStar = stars.find(
      (star) => Math.hypot(star.x - clickX, star.y - clickY) < star.radius + 5
    );
    if (clickedStar) setSelectedStar(clickedStar);
  };

  const closeCard = () => setSelectedStar(null);

  useEffect(() => {
    if (stars.length === 0) return;
    const newStars = stars.map((star, idx) => {
      const offset = (scrollY / 5) * (idx % 2 === 0 ? 1 : -1);
      return { ...star, y: star.y + offset };
    });
    setStars(newStars);
  }, [scrollY]);

  const handleLogin = () => {
    const id = document.getElementById("login-id").value;
    const pw = document.getElementById("login-password").value;
    console.log("로그인 정보:", { id, pw });
    setIsLoggedIn(true);
    navigate("/globe");
  };

  return (
    <>
      <div style={{ position: "fixed", inset: 0, background: "radial-gradient(ellipse at center, #000011 0%, #000000 80%), url('https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=1350&q=80') no-repeat center/cover", zIndex: -2 }} />
      <canvas
        ref={canvasRef}
        style={{ display: "block", position: "fixed", inset: 0, zIndex: -1 }}
        onMouseMove={(e) => {
          const rect = canvasRef.current.getBoundingClientRect();
          setMousePos({ x: e.clientX - rect.left, y: e.clientY - rect.top });
        }}
        onMouseLeave={() => setMousePos({ x: -9999, y: -9999 })}
        onClick={handleCanvasClick}
      />

      <div style={{ position: "fixed", top: 20, right: 20, color: "white", fontWeight: "bold", cursor: "pointer", fontSize: "1.1rem", userSelect: "none", zIndex: 10, display: "flex", gap: "15px" }}>
        <span onClick={() => setShowLoginBox(!showLoginBox)}>로그인</span>
        <span onClick={() => setShowRegisterBox(!showRegisterBox)}>회원가입</span>
      </div>

      {showLoginBox && (
        <div style={{ position: "fixed", top: 70, right: 20, width: 280, padding: "20px", backgroundColor: "rgba(0, 0, 30, 0.9)", color: "white", borderRadius: "12px", boxShadow: "0 0 15px rgba(255, 255, 255, 0.2)", zIndex: 99 }}>
          <h3 style={{ marginTop: 0 }}>로그인</h3>
          <label htmlFor="login-id" style={{ fontSize: 14 }}>아이디</label>
          <input id="login-id" type="text" placeholder=" 사용자명" style={{ width: "100%", padding: "8px", marginBottom: "10px", borderRadius: "6px", border: "none" }} />
          <label htmlFor="login-password" style={{ fontSize: 14 }}>비밀번호</label>
          <input id="login-password" type="password" placeholder="비밀번호 입력" style={{ width: "100%", padding: "8px", borderRadius: "6px", border: "none" }} />
          <button onClick={handleLogin} style={{ width: "100%", marginTop: "15px", padding: "10px", borderRadius: "6px", border: "none", backgroundColor: "#1e90ff", color: "white", fontWeight: "bold", cursor: "pointer" }}>로그인</button>
        </div>
      )}

      {showRegisterBox && (
        <div style={{ position: "fixed", top: 70, right: 320, width: 280, padding: "20px", backgroundColor: "rgba(0, 0, 30, 0.9)", color: "white", borderRadius: "12px", boxShadow: "0 0 15px rgba(255, 255, 255, 0.2)", zIndex: 99 }}>
          <h3 style={{ marginTop: 0 }}>회원가입</h3>
          <label htmlFor="register-id" style={{ fontSize: 14 }}>아이디</label>
          <input id="register-id" type="text" placeholder="아이디 입력" style={{ width: "100%", padding: "8px", marginBottom: "10px", borderRadius: "6px", border: "none" }} />
          <label htmlFor="register-password" style={{ fontSize: 14 }}>비밀번호</label>
          <input id="register-password" type="password" placeholder="비밀번호 입력" style={{ width: "100%", padding: "8px", borderRadius: "6px", border: "none" }} />
          <button style={{ width: "100%", marginTop: "15px", padding: "10px", borderRadius: "6px", border: "none", backgroundColor: "#32cd32", color: "white", fontWeight: "bold", cursor: "pointer" }}>회원가입</button>
        </div>
      )}

      {selectedStar && (
        <div style={{ position: "fixed", bottom: 0, left: 0, width: "100%", maxHeight: "40vh", background: "rgba(0, 0, 30, 0.85)", color: "white", boxShadow: "0 -4px 20px rgba(0,0,0,0.7)", padding: "20px 30px", fontSize: 18, overflowY: "auto", animation: "slideUp 0.3s ease forwards", zIndex: 20 }} onClick={closeCard}>
          <h2>추억카드</h2>
          <p>이 별은 x: {Math.round(selectedStar.x)}, y: {Math.round(selectedStar.y)} 위치에 있습니다.</p>
          <p>여기에 추억에 대한 상세 내용을 추가하세요.</p>
        </div>
      )}

      <style>{`
        @keyframes slideUp {
          0% { transform: translateY(100%); opacity: 0; }
          100% { transform: translateY(0); opacity: 1; }
        }
      `}</style>
    </>
  );
}