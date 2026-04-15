<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '@/api/client'
import { showAlertModal } from '@/ui/alertModal'

const router = useRouter()
const route = useRoute()

const slots = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')
const deletingId = ref('')

const formattedSlots = computed(() => {
  return slots.value.map(slot => ({
    ...slot,
    formattedDate: formatDate(slot.date),
    formattedTime: `${slot.start} - ${slot.end}`,
    formattedDuration: formatDuration(slot.duration),
    formattedAmount: formatCurrency(slot.amount, slot.currency),
    formattedType: formatType(slot.type)
  }))
})

function formatDate(dateStr) {
  if (!dateStr) return '--'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch {
    return dateStr
  }
}

function formatDuration(minutes) {
  const mins = Number(minutes)
  if (!Number.isFinite(mins) || mins <= 0) return '--'
  return `${mins} min`
}

function formatCurrency(amount, currency = 'CNY') {
  const num = Number(amount)
  if (!Number.isFinite(num)) return '--'
  try {
    return new Intl.NumberFormat('zh-CN', {
      style: 'currency',
      currency: currency || 'CNY',
      minimumFractionDigits: 2
    }).format(num)
  } catch {
    return `${num.toFixed(2)} ${currency || ''}`.trim()
  }
}

function formatType(type) {
  const normalized = String(type || '').trim().toLowerCase()
  if (normalized === 'online') return 'online'
  if (normalized === 'offline') return 'offline'
  return normalized || '--'
}

function slotDate(slot) {
  return slot?.date || ''
}

function slotStart(slot) {
  return slot?.start || ''
}

function sortSlots(rows) {
  return [...rows].sort((a, b) => {
    const dateDiff = slotDate(a).localeCompare(slotDate(b))
    if (dateDiff !== 0) return dateDiff
    return slotStart(a).localeCompare(slotStart(b))
  })
}

async function loadSlots() {
  loading.value = true
  error.value = ''
  try {
    const response = await api.specialistListSlots()
    slots.value = sortSlots(Array.isArray(response?.items) ? response.items : [])
  } catch (e) {
    error.value = e?.message || 'Failed to load slots'
    showAlertModal({ type: 'error', message: error.value })
    slots.value = []
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  if (!id) return
  if (!confirm('Are you sure you want to delete this slot?')) return
  
  deletingId.value = id
  try {
    await api.specialistDeleteSlot(id)
    await loadSlots()
    success.value = `Slot ${id} deleted successfully.`
    showAlertModal({ type: 'success', message: success.value })
  } catch (e) {
    error.value = e?.message || 'Failed to delete slot'
    showAlertModal({ type: 'error', message: error.value })
  } finally {
    deletingId.value = ''
  }
}

function handleEdit(id) {
  router.push({ name: 'specialist.slotEdit', params: { id } })
}

function goToCreate() {
  router.push({ name: 'specialist.slotCreate' })
}

onMounted(async () => {
  await loadSlots()
})
</script>

<template>
  <section class="page">
    <header class="page__header">
      <div>
        <h1>Slot Management</h1>
        <p class="subtitle">Manage your consultation slots by creating, editing, and deleting.</p>
      </div>
      <button type="button" class="btn-primary" @click="goToCreate">
        Create Slot
      </button>
    </header>

    <section class="calc-card">
      <div v-if="error" class="banner banner--error">
        {{ error }}
      </div>

      <div v-if="success" class="banner banner--success">
        {{ success }}
      </div>

      <div class="slots-table-container">
        <table class="slots-table">
          <thead>
            <tr>
              <th scope="col">SCHEDULE</th>
              <th scope="col">PRICE</th>
              <th scope="col">SESSION</th>
              <th scope="col">DETAIL</th>
              <th scope="col">AVAILABILITY</th>
              <th scope="col" class="th-actions">ACTIONS</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in formattedSlots" :key="slot.id">
              <td>
                <div>{{ slot.formattedDate }}</div>
                <div class="time">{{ slot.formattedTime }}</div>
              </td>
              <td>{{ slot.formattedAmount }}</td>
              <td>{{ slot.formattedDuration }} · {{ slot.formattedType }}</td>
              <td>{{ slot.detail || '--' }}</td>
              <td>
                <span :class="['availability-badge', slot.available ? 'available' : 'unavailable']">
                  {{ slot.available ? 'Available' : 'Unavailable' }}
                </span>
              </td>
              <td class="td-actions">
                <button 
                  type="button" 
                  class="btn-secondary" 
                  @click="handleEdit(slot.id)"
                  :disabled="loading"
                >
                  Edit
                </button>
                <button 
                  type="button" 
                  class="btn-danger" 
                  @click="handleDelete(slot.id)"
                  :disabled="loading || deletingId === slot.id"
                >
                  {{ deletingId === slot.id ? 'Deleting...' : 'Delete' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="!loading && formattedSlots.length === 0" class="empty-state">
          <p>No slots found. Click "Create Slot" to add your first slot.</p>
        </div>

        <div v-if="loading" class="loading-state">
          <p>Loading slots...</p>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.page__header {
  margin: 8px 0 20px;
  padding: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page__header h1 {
  margin: 0;
  font-size: clamp(32px, 3.1vw, 38px);
  font-weight: 800;
  line-height: 1.12;
}

.subtitle {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 14px;
}

.calc-card {
  background: #ffffff;
  border: 1px solid rgba(17, 24, 39, 0.1);
  border-radius: 0;
  padding: 16px;
  box-shadow: 0 8px 18px rgba(17, 24, 39, 0.06);
}

.banner {
  margin-bottom: 16px;
  padding: 10px 12px;
  border-radius: 0;
  font-size: 13px;
}

.banner--error {
  border: 1px solid rgba(248, 113, 113, 0.45);
  background: rgba(248, 113, 113, 0.12);
  color: #991b1b;
}

.banner--success {
  border: 1px solid rgba(16, 185, 129, 0.45);
  background: rgba(16, 185, 129, 0.12);
  color: #065f46;
}

.slots-table-container {
  overflow-x: auto;
}

.slots-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.slots-table th,
.slots-table td {
  padding: 12px 14px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.slots-table th {
  background-color: #f8f5f2;
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
}

.slots-table td {
  color: #111827;
}

.time {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.availability-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.availability-badge.available {
  background: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.availability-badge.unavailable {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.th-actions,
.td-actions {
  text-align: right;
  white-space: nowrap;
}

.td-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.btn-primary {
  height: 44px;
  padding: 0 20px;
  border: 1px solid #a94442;
  border-radius: 0;
  background: #a94442;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.18s ease;
}

.btn-primary:hover {
  background: #8b3735;
}

.btn-secondary {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #000000;
  border-radius: 0;
  background: #ffffff;
  color: #000000;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
}

.btn-secondary:hover {
  background: #f3f4f6;
}

.btn-danger {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ef4444;
  border-radius: 0;
  background: #ef4444;
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.18s ease;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-primary:disabled,
.btn-secondary:disabled,
.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-state {
  margin: 40px 0;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.loading-state {
  margin: 40px 0;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

@media (max-width: 768px) {
  .page__header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .slots-table {
    font-size: 12px;
  }

  .slots-table th,
  .slots-table td {
    padding: 10px 8px;
  }

  .td-actions {
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
  }

  .btn-secondary,
  .btn-danger {
    height: 32px;
    padding: 0 8px;
    font-size: 12px;
  }
}
</style>