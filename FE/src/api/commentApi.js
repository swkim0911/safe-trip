import apiClient from './apiClient'

const basePath = (reportType, reportId) =>
  reportType === 'USER' ? `/user-reports/${reportId}/comments` : `/external-reports/${reportId}/comments`

export const commentApi = {
  getComments(reportType, reportId) {
    return apiClient.get(basePath(reportType, reportId))
  },

  createComment(reportType, reportId, { content, parentCommentId = null }) {
    return apiClient.post(basePath(reportType, reportId), { content, parentCommentId })
  },

  updateComment(reportType, reportId, commentId, { content }) {
    return apiClient.patch(`${basePath(reportType, reportId)}/${commentId}`, { content })
  },

  deleteComment(reportType, reportId, commentId) {
    return apiClient.delete(`${basePath(reportType, reportId)}/${commentId}`)
  },
}
