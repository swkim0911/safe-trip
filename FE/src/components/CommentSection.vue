<template>
  <div class="comment-section">
    <div class="comment-section-header">
      <h6 class="comment-title">Comments</h6>
      <span class="comment-count">{{ comments.length }}</span>
    </div>

    <div v-if="isLoading" class="comment-empty-state">Loading comments...</div>

    <div v-else-if="comments.length === 0" class="comment-empty-state">
      <div class="empty-title">No comments yet</div>
      <div class="empty-copy">Share a helpful note or ask a question about this story.</div>
    </div>

    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item mb-3">
        <CommentCard
          :comment="comment"
          :current-user-nickname="authStore.user?.nickname"
          @reply="handleReply"
          @edit="handleEdit"
          @delete="handleDelete"
          @like="handleLike"
        />

        <!-- nested replies -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies ms-4 mt-2">
          <div v-for="reply in comment.replies" :key="reply.id" class="mb-2">
            <CommentCard
              :comment="reply"
              :current-user-nickname="authStore.user?.nickname"
              :is-reply="true"
              @edit="handleEdit"
              @delete="handleDelete"
              @like="handleLike"
            />
          </div>
        </div>

        <!-- inline reply input -->
        <div v-if="replyTargetId === comment.id" class="ms-4 mt-2">
          <textarea
            v-model="replyContent"
            class="form-control form-control-sm comment-input"
            rows="2"
            placeholder="Write a reply..."
            maxlength="500"
            @keydown.ctrl.enter="submitReply(comment.id)"
          ></textarea>
          <div class="d-flex justify-content-between align-items-center mt-1">
            <span class="text-muted" style="font-size: 0.75rem;">{{ replyContent.length }} / 500</span>
            <div class="d-flex gap-2">
              <button class="btn btn-sm btn-secondary" @click="cancelReply">Cancel</button>
              <button
                class="btn btn-sm comment-submit-btn"
                :disabled="!replyContent.trim() || isSubmitting"
                @click="submitReply(comment.id)"
              >
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
                Reply
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-3">
      <div v-if="authStore.accessToken">
        <textarea
          v-model="newComment"
          class="form-control form-control-sm comment-input"
          rows="2"
          placeholder="Write a comment..."
          maxlength="500"
          @keydown.ctrl.enter="submitNewComment"
        ></textarea>
        <div class="d-flex justify-content-between align-items-center mt-1">
          <span class="text-muted" style="font-size: 0.75rem;">{{ newComment.length }} / 500</span>
          <button
            class="btn btn-sm comment-submit-btn"
            :disabled="!newComment.trim() || isSubmitting"
            @click="submitNewComment"
          >
            <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
            Comment
          </button>
        </div>
      </div>
      <div v-else class="comment-login-state">
        <button class="btn btn-sm comment-login-btn" @click="emit('request-login')">
          Log in to join the conversation
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useComment } from '@/composables/useComment'
import CommentCard from './CommentCard.vue'

const props = defineProps({
  reportId: { type: Number, required: true },
  reportType: { type: String, required: true }, // 'USER' | 'EXTERNAL'
})

const emit = defineEmits(['request-login'])

const authStore = useAuthStore()
const { comments, isLoading, fetchComments, submitComment, editComment, removeComment, toggleLike } = useComment()

const newComment = ref('')
const replyTargetId = ref(null)
const replyContent = ref('')
const isSubmitting = ref(false)

// refetch when switching to a different report
watch(() => props.reportId, (id) => {
  if (id) fetchComments(props.reportType, id)
}, { immediate: true })

// refetch on login/logout to refresh likedByMe state
watch(() => authStore.accessToken, () => {
  if (props.reportId) fetchComments(props.reportType, props.reportId)
})

async function submitNewComment() {
  if (!newComment.value.trim()) return
  isSubmitting.value = true
  try {
    await submitComment(props.reportType, props.reportId, newComment.value.trim())
    newComment.value = ''
  } catch (e) {
    console.error('Failed to submit comment', e)
  } finally {
    isSubmitting.value = false
  }
}

function handleReply(commentId) {
  if (!authStore.accessToken) {
    emit('request-login')
    return
  }
  replyTargetId.value = commentId
  replyContent.value = ''
}

async function submitReply(parentCommentId) {
  if (!replyContent.value.trim()) return
  isSubmitting.value = true
  try {
    await submitComment(props.reportType, props.reportId, replyContent.value.trim(), parentCommentId)
    replyTargetId.value = null
    replyContent.value = ''
  } catch (e) {
    console.error('Failed to submit reply', e)
  } finally {
    isSubmitting.value = false
  }
}

function cancelReply() {
  replyTargetId.value = null
  replyContent.value = ''
}

async function handleEdit({ commentId, content }) {
  try {
    await editComment(props.reportType, props.reportId, commentId, content)
  } catch (e) {
    console.error('Failed to edit comment', e)
  }
}

async function handleDelete(commentId) {
  try {
    await removeComment(props.reportType, props.reportId, commentId)
  } catch (e) {
    console.error('Failed to delete comment', e)
  }
}

async function handleLike(commentId) {
  if (!authStore.accessToken) {
    emit('request-login')
    return
  }
  try {
    await toggleLike(commentId)
  } catch (e) {
    console.error('Failed to toggle like', e)
  }
}
</script>

<style scoped>
.comment-title {
  margin: 0;
  color: var(--safetrip-muted);
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.comment-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.comment-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  color: var(--safetrip-primary);
  background: var(--safetrip-primary-soft);
  font-size: 0.78rem;
  font-weight: 800;
}

.comment-empty-state {
  padding: 22px 16px;
  border: 1px dashed var(--safetrip-border);
  border-radius: 12px;
  background: #fffdf8;
  color: var(--safetrip-muted);
  text-align: center;
}

.empty-title {
  margin-bottom: 4px;
  color: var(--safetrip-text);
  font-weight: 700;
}

.empty-copy {
  font-size: 0.9rem;
}

.replies {
  border-left: 2px solid var(--safetrip-border);
  padding-left: 0.75rem;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-item {
  margin-bottom: 0 !important;
}

.comment-input {
  border-color: var(--safetrip-border);
  background: #fffdf8;
}

.comment-input:focus {
  border-color: var(--safetrip-primary);
  box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.12);
}

.comment-submit-btn {
  color: #fff;
  background: var(--safetrip-primary);
  border-color: var(--safetrip-primary);
}

.comment-submit-btn:hover,
.comment-submit-btn:focus {
  color: #fff;
  background: var(--safetrip-primary-hover);
  border-color: var(--safetrip-primary-hover);
}

.comment-submit-btn:disabled {
  background: #a7cfc8;
  border-color: #a7cfc8;
  opacity: 1;
}

.comment-login-btn {
  color: var(--safetrip-primary);
  border: 1px solid #b9d8d2;
  background: #fffdf8;
}

.comment-login-btn:hover,
.comment-login-btn:focus {
  color: #fff;
  background: var(--safetrip-primary);
  border-color: var(--safetrip-primary);
}

.comment-login-state {
  padding-top: 14px;
  text-align: center;
}
</style>
