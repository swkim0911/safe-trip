<template>
  <div class="modal fade" id="termsOfServiceModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title fw-bold">Terms of Service</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body policy-body">
          <p class="text-muted small">Effective Date: April 28, 2026</p>

          <p>Welcome to SafeTrip. By accessing or using <strong>safetrip.world</strong>, you agree to be bound by these Terms of Service. If you do not agree, please do not use the service.</p>

          <h6 class="section-title">1. What SafeTrip Is</h6>
          <p>SafeTrip is an informational platform that aggregates publicly available travel scam reports from Reddit communities and allows registered users to submit their own scam reports. It is intended to help travelers stay informed about potential risks in various destinations.</p>

          <h6 class="section-title">2. No Guarantee of Accuracy</h6>
          <p>SafeTrip does not verify the accuracy, completeness, or timeliness of any report displayed on the platform — whether sourced from Reddit or submitted by users. <strong>All content is provided for informational purposes only and should not be relied upon as the sole basis for any travel or safety decisions.</strong> SafeTrip assumes no responsibility for harm arising from reliance on its content.</p>

          <h6 class="section-title">3. User Accounts</h6>
          <p>You must provide a valid email address and a nickname to create an account. You are responsible for maintaining the confidentiality of your credentials and for all activities that occur under your account. You must not create accounts using false information or impersonate another person.</p>

          <h6 class="section-title">4. User-Submitted Content</h6>
          <p>You retain ownership of the scam reports and images you submit to SafeTrip. By submitting content, you grant SafeTrip a non-exclusive, worldwide, royalty-free license to display and distribute that content within the service.</p>
          <p>You must not submit content that is false, defamatory, hateful, or violates the rights of any third party. SafeTrip reserves the right to remove content that violates these terms without notice.</p>

          <h6 class="section-title">5. Reddit Content</h6>
          <p>Some reports on SafeTrip are derived from publicly available Reddit posts. This content is attributed to its original source and linked back to the original post. SafeTrip does not claim ownership of Reddit content. Reddit content belongs to its respective authors and is subject to Reddit's Terms of Service.</p>

          <h6 class="section-title">6. Prohibited Use</h6>
          <ul>
            <li>Using the service for any unlawful purpose</li>
            <li>Submitting spam, false reports, or misleading information</li>
            <li>Attempting to access or tamper with other users' data</li>
            <li>Automated scraping or mass data extraction without permission</li>
          </ul>

          <h6 class="section-title">7. Limitation of Liability</h6>
          <p>To the maximum extent permitted by applicable law, SafeTrip and its operators shall not be liable for any indirect, incidental, special, or consequential damages arising from your use of or inability to use the service, even if advised of the possibility of such damages.</p>

          <h6 class="section-title">8. Disclaimer of Warranties</h6>
          <p>SafeTrip is provided "as is" without warranties of any kind, either express or implied, including but not limited to warranties of merchantability, fitness for a particular purpose, or non-infringement.</p>

          <h6 class="section-title">9. Changes to These Terms</h6>
          <p>We may update these Terms of Service from time to time. Continued use of SafeTrip after changes are posted constitutes your acceptance of the updated terms.</p>

          <h6 class="section-title">10. Contact</h6>
          <p>For any questions regarding these Terms, please contact us at <a href="mailto:safetripworld.contact@gmail.com">safetripworld.contact@gmail.com</a>.</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-primary" data-bs-dismiss="modal">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.policy-body {
  font-size: 14px;
  line-height: 1.7;
  color: #333;

  p { margin-bottom: 12px; }
  ul { margin-bottom: 12px; padding-left: 1.4rem; li { margin-bottom: 4px; } }
  a { color: var(--safetrip-primary); }
}

.section-title {
  font-weight: 700;
  font-size: 14px;
  margin-top: 20px;
  margin-bottom: 8px;
  color: #111;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
</style>
