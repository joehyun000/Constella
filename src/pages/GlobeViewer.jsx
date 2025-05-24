import React, { useEffect, useRef } from 'react';
import * as THREE from 'three';
import ThreeGlobe from 'three-globe';
import { TrackballControls } from 'three/examples/jsm/controls/TrackballControls';
import { CSS2DRenderer } from 'three/examples/jsm/renderers/CSS2DRenderer';

const GlobeViewer = () => {
  const globeRef = useRef(null);

  useEffect(() => {
    const container = globeRef.current; // ref 값을 변수에 복사

    if (!container) return;

    // 마커 SVG
    const markerSvg = `<svg viewBox="-4 0 36 36">
      <path fill="currentColor" d="M14,0 C21.732,0 28,5.641 28,12.6 C28,23.963 14,36 14,36 C14,36 0,24.064 0,12.6 C0,5.641 6.268,0 14,0 Z"></path>
      <circle fill="black" cx="14" cy="14" r="7"></circle>
    </svg>`;

    // const N = 30;
    // const gData = [...Array(N).keys()].map(() => ({   //랜덤 마커 생성
    //   lat: (Math.random() - 0.5) * 180,
    //   lng: (Math.random() - 0.5) * 360,
    //   size: 7 + Math.random() * 30,
    //   color: ['red', 'white', 'blue', 'green'][Math.round(Math.random() * 3)],
    // }));
    //
    //  ? 수동 마커 3개 예시
const gData = [
  {
    lat: 37.5665, // 서울
    lng: 126.9780,
    size: 20,
    color: 'red',
    id: 'marker-1'
  },
  {
    lat: 48.8566, // 파리
    lng: 2.3522,
    size: 20,
    color: 'blue',
    id: 'marker-2'
  },
  {
    lat: 34.0522, // LA
    lng: -118.2437,
    size: 20,
    color: 'green',
    id: 'marker-3'
  }
];

    const globe = new ThreeGlobe()
      .globeImageUrl('//cdn.jsdelivr.net/npm/three-globe/example/img/earth-dark.jpg')
      .bumpImageUrl('//cdn.jsdelivr.net/npm/three-globe/example/img/earth-topology.png')
      .htmlElementsData(gData)
      .htmlElement(d => {
        const el = document.createElement('div');
        el.innerHTML = markerSvg;
        el.style.color = d.color;
        el.style.width = `${d.size}px`;
        el.style.transition = 'opacity 250ms';
        return el;
      })
      .htmlElementVisibilityModifier((el, isVisible) => {
        el.style.opacity = isVisible ? 1 : 0;
      });

    const scene = new THREE.Scene();
    scene.add(globe);
    scene.add(new THREE.AmbientLight(0xcccccc, Math.PI));
    scene.add(new THREE.DirectionalLight(0xffffff, 0.6 * Math.PI));

    const camera = new THREE.PerspectiveCamera();
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    camera.position.z = 500;

    const webglRenderer = new THREE.WebGLRenderer();
    const labelRenderer = new CSS2DRenderer();

    [webglRenderer, labelRenderer].forEach((renderer, idx) => {
      renderer.setSize(window.innerWidth, window.innerHeight);
      if (idx > 0) {
        renderer.domElement.style.position = 'absolute';
        renderer.domElement.style.top = '0px';
        renderer.domElement.style.pointerEvents = 'none';
      }
      container.appendChild(renderer.domElement);
    });

    const controls = new TrackballControls(camera, webglRenderer.domElement);
    controls.minDistance = 101;
    controls.rotateSpeed = 5;
    controls.zoomSpeed = 0.8;

    globe.setPointOfView(camera);
    controls.addEventListener('change', () => globe.setPointOfView(camera));

    const animate = () => {
      controls.update();
      webglRenderer.render(scene, camera);
      labelRenderer.render(scene, camera);
      requestAnimationFrame(animate);
    };
    animate();

    const handleResize = () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      webglRenderer.setSize(window.innerWidth, window.innerHeight);
      labelRenderer.setSize(window.innerWidth, window.innerHeight);
    };
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      if (container) {
        container.innerHTML = '';
      }
    };
  }, []);

  
  return (
    <div ref={globeRef} style={{ width: '100vw', height: '100vh', position: 'relative' }} />
  );
};

export default GlobeViewer;
