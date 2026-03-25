import { ref } from 'vue'
import { commentApi } from '@/api/commentApi'

export function useComment() {
  const comments = ref([])
  const isLoading = ref(false)

  async function fetchComments(reportType, reportId) {
    isLoading.value = true
    try {
      const res = await commentApi.getComments(reportType, reportId)
      comments.value = res.data.result
    } catch (e) {
      console.error('Failed to fetch comments', e)
    } finally {
      isLoading.value = false
    }
  }

  async function submitComment(reportType, reportId, content, parentCommentId = null) {
    await commentApi.createComment(reportType, reportId, { content, parentCommentId })
    await fetchComments(reportType, reportId)
  }

  async function editComment(reportType, reportId, commentId, content) {
    await commentApi.updateComment(reportType, reportId, commentId, { content })
    await fetchComments(reportType, reportId)
  }

  async function removeComment(reportType, reportId, commentId) {
    await commentApi.deleteComment(reportType, reportId, commentId)
    await fetchComments(reportType, reportId)
  }

  function reset() {
    comments.value = []
  }

  return { comments, isLoading, fetchComments, submitComment, editComment, removeComment, reset }
}
