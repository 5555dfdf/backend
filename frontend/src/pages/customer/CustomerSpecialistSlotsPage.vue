<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client'
import { showAlertModal } from '@/ui/alertModal'

const props = defineProps({
  id: { type: String, required: true },
  bookingId: { type: String, default: '' }
})
const router = useRouter()

const isReschedule = computed(() => !!props.bookingId)

const slots = ref([])
const slotDate = ref(new Date().toISOString().slice(0, 10))
const selectedSlotId = ref('')
const loading = ref(false)
const error = ref('')
const note = ref('')
const submitting = ref(false)

function formatSlotTime(value) {
  const raw = String(value ?? '').trim()
  if (!raw) return '—'

  const timeMatch = raw.match(/T?(\d{2}:\d{2})/)
  if (timeMatch) return timeMatch[1]

  return raw
}

function formatSlotRange(slot) {
  return `${formatSlotTime(slot?.start ?? slot?.startTime)} - ${formatSlotTime(slot?.end ?? slot?.endTime)}`
}

function formatMoney(slot) {
  const amount = Number(slot?.amount ?? 0)
  const safeAmount = Number.isNaN(amount) ? 0 : amount
  const currency = String(slot?.currency ?? 'CNY').trim() || 'CNY'
  return `${safeAmount.toFixed(2)} ${currency}`
}

function formatSession(slot) {
  const duration = Number(slot?.duration ?? 0)
  const safeDuration = Number.isNaN(duration) || duration <= 0 ? '--' : `${duration} min`
  const type = String(slot?.type ?? 'online').trim() || 'online'
  return `${safeDuration} · ${type}`
}

function formatDetail(slot) {
  return String(slot?.detail ?? '').trim() || '--'
}

async function loadSlots() {
  if (!props.id) return

  loading.value = true
  error.value = ''

  try {
    slots.value = await api.listSpecialistSlots(props.id, { date: slotDate.value })
    selectedSlotId.value = ''
  } catch (e) {
    slots.value = []
    error.value = e?.message || 'Failed to load slots'
  } finally {
    loading.value = false
  }
}

async function submitBooking() {
  if (!props.id) {
    error.value = 'Missing specialistId'
    showAlertModal({ type: 'error', message: error.value })
    return
  }
  if (!selectedSlotId.value) {
    error.value = 'Please select a slot first'
    showAlertModal({ type: 'error', message: error.value })
    return
  }

  submitting.value = true
  error.value = ''
  try {
    if (isReschedule.value) {
      await api.rescheduleBooking(props.bookingId, { slotId: selectedSlotId.value })
      selectedSlotId.value = ''
      showAlertModal({
        type: 'success',
        message: 'Rescheduled successfully.',
        onClose: () => router.push({ name: 'customer.bookingDetail', params: { id: props.bookingId } })
      })
    } else {
      await api.createBooking({
        specialistId: props.id,
        slotId: selectedSlotId.value,
        note: note.value.trim() || undefined
      })
      note.value = ''
      selectedSlotId.value = ''
      showAlertModal({
        type: 'success',
        message: 'Booking request submitted successfully.',
        onClose: () => router.push({ name: 'customer.bookings' })
      })
    }
    await loadSlots()
  } catch (e) {
    error.value = e?.message || (isReschedule.value ? 'Failed to reschedule' : 'Failed to submit booking')
    showAlertModal({ type: 'error', message: error.value })
  } finally {
    submitting.value = false
  }
}

watch(
    () => props.id,
    () => loadSlots(),
    { immediate: true }
)

watch(slotDate, () => loadSlots())

defineExpose({
  selectedSlotId,
  getSelectedSlot: () => slots.value.find((sl) => (sl.slotId ?? sl.id) === selectedSlotId.value)
})
</script>

<template>
  <section class="page">
    <header class="page__header">
      <h1>{{ isReschedule ? 'Reschedule - Choose a New Slot' : 'Specialist Available Slots' }}</h1>
      <p class="subtitle">
        {{ isReschedule ? 'Choose a new available time slot to reschedule this booking.' : 'Choose a date and an available time slot to submit your booking request.' }}
      </p>
    </header>

    <div v-if="error" class="banner banner--error" role="alert">{{ error }}</div>
    <div v-else-if="loading" class="card muted">Loading slots...</div>

    <template v-else>
      <div class="card">
        <div class="title">Available Slots</div>

        <label class="field">
          <span class="label">Date</span>
          <input v-model="slotDate" type="date" lang="en" class="input" />
        </label>

        <ul v-if="slots.length" class="slots">
          <li v-for="sl in slots" :key="sl.slotId ?? sl.id" class="slot-row">
            <label class="pick">
              <input
                  v-model="selectedSlotId"
                  type="radio"
                  name="slot"
                  :value="sl.slotId ?? sl.id"
                  :disabled="sl.available === false"
              />
              <div class="slot-main">
                <span class="slot-time">{{ formatSlotRange(sl) }}</span>
                <span class="slot-meta">{{ formatMoney(sl) }} · {{ formatSession(sl) }}</span>
                <span class="slot-meta slot-meta--detail" :title="formatDetail(sl)">Detail: {{ formatDetail(sl) }}</span>
              </div>
              <span v-if="sl.available === false" class="muted small">(Full)</span>
            </label>
          </li>
        </ul>

        <p v-else class="muted small">No available slots for this date.</p>

        <label v-if="!isReschedule" class="field field-note">
          <span class="label">Note (optional)</span>
          <textarea
            v-model="note"
            class="input input--area"
            rows="3"
            maxlength="300"
            placeholder="Tell the specialist any context for this booking."
          ></textarea>
        </label>

        <button
          type="button"
          class="btn-submit"
          :disabled="submitting || !selectedSlotId"
          @click="submitBooking"
        >
          {{ submitting ? 'Submitting...' : isReschedule ? 'Submit Reschedule' : 'Submit Booking Request' }}
        </button>
      </div>
    </template>
  </section>
</template>

<style scoped>
.page__header h1 {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 800;
}

.subtitle {
  margin: 0;
  color: #5b6472;
  font-size: 14px;
}

.muted {
  opacity: 0.8;
}

.small {
  font-size: 12px;
}

.mono {
  font-family: ui-monospace, monospace;
  font-size: 13px;
}

.card {
  margin-top: 14px;
  padding: 16px;
  border: 1px solid #e6e8ef;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.04);
}

.title {
  font-weight: 700;
  margin-bottom: 10px;
  font-size: 18px;
}

.field {
  display: grid;
  gap: 6px;
  margin-bottom: 10px;
}

.label {
  font-size: 13px;
  color: #4b5563;
  font-weight: 600;
}

.input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #d3d8e1;
  background: #ffffff;
  color: #111827;
  outline: none;
}

.input--area {
  min-height: 92px;
  resize: vertical;
}

.slots {
  list-style: none;
  padding: 0;
  margin: 8px 0 0;
  display: grid;
  gap: 6px;
}

.pick {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  cursor: pointer;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #e6e8ef;
  background: #f8fafc;
}

.slot-main {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.slot-time {
  font-weight: 700;
  color: #111827;
}

.slot-meta {
  font-size: 12px;
  color: #4b5563;
}

.slot-meta--detail {
  max-width: 560px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.banner {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: 0;
  font-size: 13px;
}

.banner--error {
  border: 1px solid rgba(248, 113, 113, 0.45);
  background: rgba(248, 113, 113, 0.12);
  color: #991b1b;
}

.field-note {
  margin-top: 12px;
}

.btn-submit {
  margin-top: 10px;
  width: 100%;
  max-width: 260px;
  height: 42px;
  border: none;
  border-radius: 10px;
  background: #07c160;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.btn-submit:hover:not(:disabled) {
  background: #06ad56;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>