import React, { useEffect, useRef, useState } from "react";

const STAR_COUNT = 100;
const STAR_RADIUS = 1.5;
const CONSTELLATION_LINE_DISTANCE = 100;

function createStars(width, height) {
  let stars = [];
  for (let i = 0; i < STAR_COUNT; i++) {
    stars.push({
      x: Math.random() * width,
      y: Math.random() * height,
      radius: Math.random() * STAR_RADIUS + 0.5,
      twinkleSpeed: 0.01 + Math.random() * 0.02,
      opacity: Math.random(),
    });
  }
  return stars;
}

export default function Home({ setIsLoggedIn }) {
  const canvasRef = useRef(null);
  const [stars, setStars] = useState([]);
  const [mousePos, setMousePos] = useState({ x: -9999, y: -9999 });
  const [selectedStar, setSelectedStar] = useState(null);
  const [scrollY, setScrollY] = useState(0);

  useEffect(() => {
    const handleResize = () => {
      const width = window.innerWidth;
      const height = window.innerHeight;
      setStars(createStars(width, height));
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
    const onScroll = () => {
      setScrollY(window.scrollY);
    };
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

      // ���ڸ� �� �׸���
      ctx.strokeStyle = "rgba(255, 255, 255, 0.2)";
      ctx.lineWidth = 0.8;
      ctx.beginPath();
      for (let i = 0; i < stars.length; i++) {
        for (let j = i + 1; j < stars.length; j++) {
          const dx = stars[i].x - stars[j].x;
          const dy = stars[i].y - stars[j].y;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < CONSTELLATION_LINE_DISTANCE) {
            ctx.moveTo(stars[i].x, stars[i].y);
            ctx.lineTo(stars[j].x, stars[j].y);
          }
        }
      }
      // ���콺 ��ó ���ڸ� �� �׸���
      stars.forEach((star) => {
        const dx = star.x - mousePos.x;
        const dy = star.y - mousePos.y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < CONSTELLATION_LINE_DISTANCE) {
          ctx.moveTo(star.x, star.y);
          ctx.lineTo(mousePos.x, mousePos.y);
        }
      });
      ctx.stroke();

      // �� ��¦�� �׸���
      stars.forEach((star) => {
        star.opacity += star.twinkleSpeed;
        if (star.opacity >= 1 || star.opacity <= 0.3) {
          star.twinkleSpeed = -star.twinkleSpeed;
        }
        ctx.beginPath();
        ctx.fillStyle = `rgba(255, 255, 255, ${star.opacity})`;
        ctx.shadowColor = "white";
        ctx.shadowBlur = 4;
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
    if (clickedStar) {
      setSelectedStar(clickedStar);
    }
  };

  const closeCard = () => {
    setSelectedStar(null);
  };

  useEffect(() => {
    if (stars.length === 0) return;
    const newStars = stars.map((star, idx) => {
      const offset = (scrollY / 5) * (idx % 2 === 0 ? 1 : -1);
      return { ...star, y: star.y + offset };
    });
    setStars(newStars);
  }, [scrollY]);

  // �α��� ó�� (�ӽ�)
  const handleLogin = () => {
    setIsLoggedIn(true);
  };

  return (
    <>
      {/* ��� ���� �̹��� */}
      <div
        style={{
          position: "fixed",
          inset: 0,
          background:
            "radial-gradient(ellipse at center, #000011 0%, #000000 80%), url('https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=1350&q=80') no-repeat center/cover",
          zIndex: -2,
        }}
      />

      {/* ĵ���� */}
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

      {/* �α���/ȸ������ */}
      <div
        style={{
          position: "fixed",
          top: 20,
          right: 20,
          color: "white",
          fontWeight: "bold",
          cursor: "pointer",
          fontSize: "1.1rem",
          userSelect: "none",
          zIndex: 10,
          display: "flex",
          gap: "15px",
        }}
      >
        <span onClick={handleLogin}>�α���</span>
        <span>ȸ������</span>
      </div>

      {/* ���õ� �� ī�� */}
      {selectedStar && (
        <div
          style={{
            position: "fixed",
            bottom: 0,
            left: 0,
            width: "100%",
            maxHeight: "40vh",
            background: "rgba(0, 0, 30, 0.85)",
            color: "white",
            boxShadow: "0 -4px 20px rgba(0,0,0,0.7)",
            padding: "20px 30px",
            fontSize: 18,
            overflowY: "auto",
            animation: "slideUp 0.3s ease forwards",
            zIndex: 20,
          }}
          onClick={closeCard}
        >
          <h2>�߾�ī���</h2>
          <p>
            �� ���� x: {Math.round(selectedStar.x)}, y: {Math.round(selectedStar.y)} ��ġ�� �ֽ��ϴ�.
          </p>
          <p>���⿡ �߾￡ ���� �� ������ �߰��ϼ���.</p>
          <p>(Ŭ�� �� �ݱ�)</p>
        </div>
      )}

      {/* �����̵�� �ִϸ��̼� ��Ÿ�� */}
      <style>{`
        @keyframes slideUp {
          0% {
            transform: translateY(100%);
            opacity: 0;
          }
          100% {
            transform: translateY(0);
            opacity: 1;
          }
        }
      `}</style>
    </>
  );
}
