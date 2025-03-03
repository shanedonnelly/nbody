<template>
  <div class="galaxy-container">
    <!-- Contrôles placés au-dessus, plus compacts -->
    <div class="controls-bar">
      <div class="status-group">
        <div class="status" :class="connectionStatus">
          {{ connectionStatus === 'connected' ? 'Connecté' : 'Déconnecté' }}
        </div>
        <div class="message">{{ message }}</div>
      </div>
      
      <div class="instructions">
        Clic gauche: ajouter des particules • Clic droit: supprimer des particules
      </div>
    </div>
    
    <!-- Canvas plein écran -->
    <div class="canvas-container">
      <canvas 
        ref="simulationCanvas" 
        class="simulation-canvas" 
        :width="canvasWidth" 
        :height="canvasHeight"
        @contextmenu.prevent="onRightClick" 
        @click="onLeftClick"
      ></canvas>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const simulationCanvas = ref(null)
const message = ref('Simulation galactique')
const canvasWidth = ref(800)
const canvasHeight = ref(400)
const connectionStatus = ref('disconnected')
const bodyPositions = ref([])

// Constantes de la grille
const GRID_WIDTH = 400
const GRID_HEIGHT = 200

// Taille des particules
const PARTICLE_SIZE = 1.0 
const GLOW_SIZE = 1.8     

let socket = null
let animationFrameId = null

// Fonction pour calculer la taille optimale du canvas en mode paysage
const calculateCanvasSize = () => {
  const viewportHeight = window.innerHeight
  const viewportWidth = window.innerWidth
  
  // Réserver de l'espace pour les contrôles (en haut)
  const controlsHeight = 40 // hauteur réduite des contrôles
  const availableHeight = viewportHeight - controlsHeight - 8 // padding minimal
  
  // Assurer que le canvas prend presque tout l'espace disponible
  const height = availableHeight
  
  // La largeur est calculée pour conserver le ratio de la grille
  const aspectRatio = GRID_WIDTH / GRID_HEIGHT
  const width = Math.max(height * aspectRatio, viewportWidth)
  
  canvasWidth.value = width
  canvasHeight.value = height
}

// Fonction pour dessiner la simulation
const drawSimulation = () => {
  if (!simulationCanvas.value) return
  
  const ctx = simulationCanvas.value.getContext('2d')
  
  // Calcul des échelles de conversion
  const scaleX = canvasWidth.value / GRID_WIDTH
  const scaleY = canvasHeight.value / GRID_HEIGHT
  
  // Effacer le canvas avec un fond noir profond
  ctx.fillStyle = '#000011' // Noir légèrement bleuté pour un effet espace
  ctx.fillRect(0, 0, canvasWidth.value, canvasHeight.value)
  
  // Dessiner une grille très subtile en arrière-plan
  ctx.strokeStyle = 'rgba(30, 30, 60, 0.1)'
  ctx.lineWidth = 0.2
  
  const gridStep = 40
  for (let x = 0; x < GRID_WIDTH; x += gridStep) {
    ctx.beginPath()
    ctx.moveTo(x * scaleX, 0)
    ctx.lineTo(x * scaleX, canvasHeight.value)
    ctx.stroke()
  }
  
  for (let y = 0; y < GRID_HEIGHT; y += gridStep) {
    ctx.beginPath()
    ctx.moveTo(0, y * scaleY)
    ctx.lineTo(canvasWidth.value, y * scaleY)
    ctx.stroke()
  }
  
  // Dessiner chaque corps comme un cercle lumineux
  for (const pos of bodyPositions.value) {
    const x = pos.x * scaleX
    const y = pos.y * scaleY
    
    // Effet de lueur
    const size = PARTICLE_SIZE * Math.min(scaleX, scaleY)
    const glowSize = GLOW_SIZE * size
    
    const gradient = ctx.createRadialGradient(x, y, 0, x, y, glowSize)
    gradient.addColorStop(0, 'rgba(255, 255, 255, 1)')
    gradient.addColorStop(0.4, 'rgba(200, 220, 255, 0.8)')
    gradient.addColorStop(1, 'rgba(80, 100, 255, 0)')
    
    ctx.beginPath()
    ctx.fillStyle = gradient
    ctx.arc(x, y, glowSize, 0, Math.PI * 2)
    ctx.fill()
  }
  
  // Animation continue
  animationFrameId = requestAnimationFrame(drawSimulation)
}

// Fonction pour traiter les données binaires
const processBodyPositions = (buffer) => {
  const view = new DataView(buffer)
  const bodyCount = view.getInt32(0, false) // Big-endian
  
  const newPositions = []
  for (let i = 0; i < bodyCount; i++) {
    const x = view.getInt16(4 + i * 4, false) // Big-endian
    const y = view.getInt16(4 + i * 4 + 2, false) // Big-endian
    newPositions.push({ x, y })
  }
  
  bodyPositions.value = newPositions
}

// Connexion à la WebSocket
const connectWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/websocket`
  
  socket = new WebSocket(wsUrl)
  
  socket.onopen = () => {
    connectionStatus.value = 'connected'
    message.value = 'Simulation en cours...'
  }
  
  socket.onmessage = (event) => {
    if (event.data instanceof Blob) {
      // Données binaires
      event.data.arrayBuffer().then(buffer => {
        processBodyPositions(buffer)
      })
    } else {
      // Messages texte (pour la compatibilité ou les commandes)
      console.log('Message texte reçu:', event.data)
    }
  }
  
  socket.onclose = () => {
    connectionStatus.value = 'disconnected'
    message.value = 'Déconnecté'
  }
  
  socket.onerror = (error) => {
    connectionStatus.value = 'disconnected'
    message.value = 'Erreur de connexion'
    console.error('WebSocket error:', error)
  }
}

function onRightClick(event) {
  message.value = "Suppression de particules"
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send("delete")
  }
}

function onLeftClick(event) {
  message.value = "Ajout de particules"
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send("add")
  }
}

// Gestion du redimensionnement de la fenêtre
const handleResize = () => {
  calculateCanvasSize()
}

// Mise en place des événements du cycle de vie
onMounted(() => {
  // Ajouter des styles globaux pour réellement prendre tout l'écran
  document.documentElement.style.margin = '0';
  document.documentElement.style.padding = '0';
  document.documentElement.style.overflow = 'hidden';
  document.body.style.margin = '0';
  document.body.style.padding = '0';
  document.body.style.overflow = 'hidden';
  document.body.style.backgroundColor = '#000';
  
  calculateCanvasSize()
  connectWebSocket()
  window.addEventListener('resize', handleResize)
  drawSimulation() // Démarrer la boucle de rendu
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (socket) {
    socket.close()
  }
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
})
</script>

<style>
/* Styles globaux pour supprimer toutes les marges par défaut */
html, body {
  margin: 0;
  padding: 0;
  overflow: hidden;
  width: 100%;
  height: 100%;
  background-color: #000;
}
</style>

<style scoped>
.galaxy-container {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background-color: #000;
  color: #fff;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
}

.canvas-container {
  flex: 1;
  width: 100%;
  height: calc(100vh - 40px); /* Hauteur restante après la barre de contrôle */
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.controls-bar {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
  padding: 5px;
  height: 40px;
  max-height: 40px;
  background-color: rgba(0, 0, 10, 0.7);
  z-index: 10;
  box-sizing: border-box;
}

.status-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status {
  padding: 3px 6px;
  border-radius: 3px;
  font-weight: bold;
  font-size: 0.8rem;
}

.connected {
  background-color: #2a6e2d;
  color: white;
}

.disconnected {
  background-color: #8b2525;
  color: white;
}

.message {
  font-size: 0.9rem;
  color: #aac;
}

.instructions {
  font-size: 0.85rem;
  color: #8888aa;
  white-space: nowrap;
}

.simulation-canvas {
  background-color: #000011;
  border: none;
  display: block;
  width: 100%;
  height: 100%;
  image-rendering: crisp-edges;
}

@media (max-width: 768px) {
  .controls-bar {
    flex-direction: column;
    height: auto;
    padding: 3px;
  }
  
  .status-group {
    margin-bottom: 2px;
  }
  
  .instructions {
    font-size: 0.7rem;
  }
}
</style>