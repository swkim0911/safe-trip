<template>
  <div class="comment-section mt-3">
    <h6 class="fw-bold mb-3">Comments</h6>

    <!-- 댓글 목록 -->
    <div v-if="isLoading" class="text-center text-muted small py-2">Loading comments...</div>

    <div v-else-if="comments.length === 0" class="text-center text-muted small py-2">
      No comments yet. Be the first to comment!
    </div>

    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item mb-3">
        <!-- 최상위 댓글 -->
        <CommentCard
          :comment="comment"
          :current-user-nickname="authStore.user?.nickname"
          @reply="handleReply"
          @edit="handleEdit"
          @delete="handleDelete"
        />

        <!-- 대댓글 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies ms-4 mt-2">
          <div v-for="reply in comment.replies" :key="reply.id" class="mb-2">
            <CommentCard
              :comment="reply"
              :current-user-nickname="authStore.user?.nickname"
              :is-reply="true"
              @edit="handleEdit"
              @delete="handleDelete"
            />
          </div>
        </div>

        <!-- 인라인 답글 입력창 -->
        <div v-if="replyTargetId === comment.id" class="ms-4 mt-2">
          <textarea
            v-model="replyContent"
            class="form-control form-control-sm"
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
                class="btn btn-sm btn-primary"
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

    <!-- 댓글 입력창 -->
    <div class="mt-3">
      <div v-if="authStore.accessToken">
        <textarea
          v-model="newComment"
          class="form-control form-control-sm"
          rows="2"
          placeholder="Write a comment..."
          maxlength="500"
          @keydown.ctrl.enter="submitNewComment"
        ></textarea>
        <div class="d-flex justify-content-between align-items-center mt-1">
          <span class="text-muted" style="font-size: 0.75rem;">{{ newComment.length }} / 500</span>
          <button
            class="btn btn-sm btn-primary"
            :disabled="!newComment.trim() || isSubmitting"
            @click="submitNewComment"
          >
            <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
            Comment
          </button>
        </div>
      </div>
      <div v-else class="text-center text-muted small py-2">
        <button class="btn btn-sm btn-outline-primary" @click="emit('request-login')">
          Login to write a comment
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useComment } from '@/composables/useComment'
import CommentCard from './CommentCard.vue'

const props = defineProps({
  reportId: { type: Number, required: true },
  reportType: { type: String, required: true }, // 'USER' | 'EXTERNAL'
})

const emit = defineEmits(['request-login'])

const authStore = useAuthStore()
const { comments, isLoading, fetchComments, submitComment, editComment, removeComment } = useComment()

const newComment = ref('')
const replyTargetId = ref(null)
const replyContent = ref('')
const isSubmitting = ref(false)

onMounted(() => fetchComments(props.reportType, props.reportId))
watch(() => props.reportId, (id) => {
  if (id) fetchComments(props.reportType, id)
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
</script>

<style scoped>
.replies {
  border-left: 2px solid #e9ecef;
  padding-left: 0.75rem;
}
</style>
