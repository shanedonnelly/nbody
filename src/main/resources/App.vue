<template>
  <div class="container">
    <div 
      class="grid" 
      :style="{
        gridTemplateColumns: `repeat(${gridSize}, ${cellSize}px)`,
        width: `${gridSize * cellSize}px`,
        height: `${gridSize * cellSize}px`
      }"
    >
      <div
        v-for="(cell, index) in cells"
        :key="index"
        :style="{ backgroundColor: cell, width: `${cellSize}px`, height: `${cellSize}px` }"
        class="cell"
        @contextmenu.prevent="onRightClick(Math.floor(index/gridSize), index%gridSize)" 
        @click="onLeftClick(Math.floor(index/gridSize), index%gridSize)"
      ></div>
    </div>
    <div class="frame">
      {{ message }}
    </div>
    <div class="status" :class="connectionStatus">
      {{ connectionStatus === 'connected' ? 'Connecté' : 'Déconnecté' }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const cells = ref([]) 
const message = ref('Cliquez sur la grille')
const gridSize = ref(0)
const cellSize = ref(0)
const connectionStatus = ref('disconnected')
let socket = null

// Fonction qui calcule la taille des cellules pour occuper 80% de la hauteur
const calculateCellSize = (size) => {
  const viewportHeight = window.innerHeight
  const targetHeight = viewportHeight * 0.8
  return Math.floor(targetHeight / size)
}

// Fonction pour initialiser la grille à partir du premier message
const initializeGrid = (data) => {
  const lines = data.split('\n').filter(line => line.trim() !== '')
  gridSize.value = lines.length
  cellSize.value = calculateCellSize(gridSize.value)
  
  // Création d'un tableau 1D à partir des données 2D
  const newCells = []
  for (let i = 0; i < lines.length; i++) {
    for (let j = 0; j < lines[i].length; j++) {
      newCells.push(lines[i][j] === '1' ? 'white' : 'black')
    }
  }
  cells.value = newCells
}

// Fonction pour mettre à jour la grille avec les messages suivants
const updateGrid = (data) => {
  const lines = data.split('\n').filter(line => line.trim() !== '')
  
  // Vérification que la taille correspond
  if (lines.length !== gridSize.value) {
    console.error('La taille du message ne correspond pas à la taille de la grille')
    return
  }
  
  // Mise à jour des cellules
  for (let i = 0; i < lines.length; i++) {
    for (let j = 0; j < lines[i].length && j < gridSize.value; j++) {
      const index = i * gridSize.value + j
      cells.value[index] = lines[i][j] === '1' ? 'white' : 'black'
    }
  }
}

// Connexion à la WebSocket
const connectWebSocket = () => {
  // const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  // const wsUrl = `${protocol}//${window.location.host}/websocket`
  const wsUrl = 'ws://localhost:8080/websocket'
  
  socket = new WebSocket(wsUrl)
  
  socket.onopen = () => {
    connectionStatus.value = 'connected'
    message.value = 'Connecté à la WebSocket'
  }
  
  socket.onmessage = (event) => {
    if (gridSize.value === 0) {
      // Premier message - initialisation
      initializeGrid(event.data)
    } else {
      // Messages suivants - mise à jour
      updateGrid(event.data)
    }
  }
  
  socket.onclose = () => {
    connectionStatus.value = 'disconnected'
    message.value = 'Déconnecté de la WebSocket'
  }
  
  socket.onerror = (error) => {
    connectionStatus.value = 'disconnected'
    message.value = 'Erreur de connexion WebSocket'
    console.error('WebSocket error:', error)
  }
}

function onRightClick(row, col) {
  message.value = "clic droit"
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send("delete")
  }
}

function onLeftClick(row, col) {
  message.value = "clic gauche"
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send("add")
  }
}

// Gestion du redimensionnement de la fenêtre
const handleResize = () => {
  if (gridSize.value > 0) {
    cellSize.value = calculateCellSize(gridSize.value)
  }
}

// Mise en place des événements du cycle de vie
onMounted(() => {
  connectWebSocket()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (socket) {
    socket.close()
  }
})
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
}

.grid {
  display: grid;
  gap: 0;
  margin: auto;
}

.cell {
  background-color: gray;
}

.frame {
  margin-top: 20px;
  border: 1px solid black;
  padding: 10px;
  width: fit-content;
  user-select: none;
}

.status {
  margin-top: 10px;
  padding: 5px 10px;
  border-radius: 4px;
  font-weight: bold;
}

.connected {
  background-color: #4caf50;
  color: white;
}

.disconnected {
  background-color: #f44336;
  color: white;
}
</style>
