import { reactive, readonly } from 'vue'

const state = reactive({
    open: false,
    title: '',
    message: '',
    onConfirm: null,
    onCancel: null
})

export function showConfirmModal(payload) {
    const p = payload ?? {}
    state.title = p.title || '确认操作'
    state.message = p.message || '您确定要执行此操作吗？'
    state.onConfirm = typeof p.onConfirm === 'function' ? p.onConfirm : null
    state.onCancel = typeof p.onCancel === 'function' ? p.onCancel : null
    state.open = true
}

export function confirmAction() {
    const cb = state.onConfirm
    state.open = false
    if (cb) cb()
}

export function cancelAction() {
    const cb = state.onCancel
    state.open = false
    if (cb) cb()
}

export function useConfirmModalState() {
    return readonly(state)
}