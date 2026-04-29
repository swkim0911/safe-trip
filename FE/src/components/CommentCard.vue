<template>
  <div class="comment-card" :class="{ reply: isReply }">
    <!-- 삭제된 댓글 -->
    <div v-if="comment.isDeleted" class="text-muted small fst-italic">
      This comment has been deleted.
    </div>

    <!-- 일반 댓글 -->
    <template v-else>
      <div class="d-flex justify-content-between align-items-start">
        <div class="flex-grow-1">
          <span class="fw-semibold small">{{ comment.authorNickname }}</span>
          <span class="text-muted small ms-2">{{ formattedDate }}</span>
        </div>

        <!-- 수정/삭제 (본인 댓글만) -->
        <div v-if="isOwner" class="d-flex gap-1 ms-2">
          <button
            v-if="!isEditing"
            class="comment-action-btn"
            @click="startEdit"
          >Edit</button>
          <button
            class="comment-action-btn danger"
            @click="confirmDelete"
          >Delete</button>
        </div>
      </div>

      <!-- 수정 모드 -->
      <div v-if="isEditing" class="mt-1">
        <textarea
          v-model="editContent"
          class="form-control form-control-sm"
          rows="2"
          maxlength="500"
        ></textarea>
        <div class="d-flex justify-content-between align-items-center mt-1">
          <span class="text-muted" style="font-size: 0.75rem;">{{ editContent.length }} / 500</span>
          <div class="d-flex gap-2">
            <button class="btn btn-sm btn-secondary" @click="cancelEdit">Cancel</button>
            <button
              class="btn btn-sm comment-save-btn"
              :disabled="!editContent.trim()"
              @click="saveEdit"
            >Save</button>
          </div>
        </div>
      </div>

      <!-- 내용 -->
      <p v-else class="mb-1 small mt-1" style="white-space: pre-wrap; word-break: break-word;">
        {{ comment.content }}
      </p>

      <!-- 액션 버튼 -->
      <div v-if="!isEditing" class="d-flex align-items-center gap-3 mt-1">
        <button
          class="comment-action-btn"
          :class="{ liked: comment.likedByMe }"
          @click="emit('like', comment.id)"
        >
          <font-awesome-icon :icon="['fas', 'thumbs-up']" />
          {{ comment.likedByMe ? 'Liked' : 'Like' }} {{ comment.likeCnt }}
        </button>
        <button
          v-if="!isReply"
          class="comment-action-btn"
          @click="emit('reply', comment.id)"
        >Reply</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  comment: { type: Object, required: true },
  currentUserNickname: { type: String, default: null },
  isReply: { type: Boolean, default: false },
})

const emit = defineEmits(['reply', 'edit', 'delete', 'like'])

const isEditing = ref(false)
const editContent = ref('')

const isOwner = computed(() =>
  props.currentUserNickname && props.currentUserNickname === props.comment.authorNickname
)

const formattedDate = computed(() => formatDate(props.comment.createdAt))

function startEdit() {
  editContent.value = props.comment.content
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
}

function saveEdit() {
  if (!editContent.value.trim()) return
  emit('edit', { commentId: props.comment.id, content: editContent.value.trim() })
  isEditing.value = false
}

function confirmDelete() {
  if (confirm('Delete this comment?')) {
    emit('delete', props.comment.id)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const now = new Date()
  const date = new Date(dateStr)
  const diffMs = now - date
  const diffMin = Math.floor(diffMs / 60000)
  const diffHour = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHour / 24)

  if (diffMin < 1) return 'just now'
  if (diffMin < 60) return `${diffMin} minute${diffMin > 1 ? 's' : ''} ago`
  if (diffHour < 24) return `${diffHour} hour${diffHour > 1 ? 's' : ''} ago`
  if (diffDay < 30) return `${diffDay} day${diffDay > 1 ? 's' : ''} ago`
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>

<style scoped>
.comment-card {
  padding: 12px 14px;
  border: 1px solid var(--safetrip-border);
  border-radius: 10px;
  background: #fffdf8;
}

.comment-card.reply {
  background: #fbfaf5;
}

.comment-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--safetrip-muted);
  font-size: 0.76rem;
  font-weight: 700;
}

.comment-action-btn:hover,
.comment-action-btn:focus {
  color: var(--safetrip-primary);
}

.comment-action-btn.danger:hover,
.comment-action-btn.danger:focus {
  color: #b6523b;
}

.comment-action-btn.liked {
  color: #b6523b;
}

.comment-save-btn {
  color: #fff;
  background: var(--safetrip-primary);
  border-color: var(--safetrip-primary);
}

.comment-save-btn:hover,
.comment-save-btn:focus {
  color: #fff;
  background: var(--safetrip-primary-hover);
  border-color: var(--safetrip-primary-hover);
}
</style>
