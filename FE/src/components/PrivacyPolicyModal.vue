<template>
  <div class="modal fade" id="privacyPolicyModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title fw-bold">Privacy Policy</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body policy-body">
          <p class="text-muted small">Effective Date: April 28, 2026</p>

          <p>SafeTrip ("we", "our", or "us") operates the website <strong>safetrip.world</strong>. This Privacy Policy explains how we collect, use, and protect your information when you use our service.</p>

          <h6 class="section-title">1. Information We Collect</h6>
          <p><strong>Account information:</strong> When you register, we collect your email address and nickname. Your password is stored as a one-way cryptographic hash and is never readable by us.</p>
          <p><strong>User-submitted content:</strong> Scam reports, descriptions, and images you voluntarily submit.</p>
          <p><strong>Usage data:</strong> We use Google Analytics to collect anonymous data about how visitors interact with SafeTrip, including pages visited, time on site, browser type, and approximate geographic region (country/city level). This data does not identify you personally.</p>

          <h6 class="section-title">2. How We Use Your Information</h6>
          <p><strong>We do not sell, rent, or share your personal information with third parties for their marketing purposes.</strong></p>
          <ul>
            <li>To provide and operate the SafeTrip service</li>
            <li>To allow you to submit and manage your scam reports</li>
            <li>To understand how the service is used and improve it</li>
            <li>To respond to your feedback or support requests</li>
          </ul>

          <h6 class="section-title">3. Third-Party Services</h6>
          <p><strong>Google Analytics:</strong> We use Google Analytics to analyze site traffic. Google may collect data according to its own <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer">Privacy Policy</a>. You may opt out using the Google Analytics Opt-out Browser Add-on. If you decline cookies on SafeTrip, Google Analytics will not be loaded.</p>
          <p><strong>Reddit public data:</strong> Some scam reports displayed on SafeTrip are sourced from publicly available Reddit posts and comments. This data is attributed to its original source and a link to the original Reddit post is provided. We do not collect any personal information from Reddit users.</p>
          <p><strong>OCI Object Storage:</strong> Images you upload are stored on Oracle Cloud Infrastructure Object Storage.</p>

          <h6 class="section-title">4. Cookies</h6>
          <p>We use a single functional cookie (<code>refreshToken</code>) to keep you logged in. This cookie is HttpOnly and Secure, and is not accessible by JavaScript. We also use Google Analytics if you have given your consent. Your analytics consent preference is stored in your browser's local storage. You can withdraw consent at any time using the <strong>Cookie Settings</strong> button at the bottom of the page.</p>

          <h6 class="section-title">5. Data Retention</h6>
          <p>Your account data is retained for as long as your account remains active. You can delete your account at any time from <strong>My Page → Profile → Delete Account</strong>. This will permanently remove your account and all associated data.</p>

          <h6 class="section-title">6. Your Rights</h6>
          <p>Depending on your location, you may have the right to:</p>
          <ul>
            <li>Access the personal data we hold about you</li>
            <li>Request correction of inaccurate data</li>
            <li>Request deletion of your data (Right to Erasure / Right to be Forgotten)</li>
            <li>Withdraw consent for analytics cookies at any time</li>
          </ul>
          <p>To exercise any of these rights, contact us at <a href="mailto:safetripworld.contact@gmail.com">safetripworld.contact@gmail.com</a>.</p>

          <h6 class="section-title">7. Children's Privacy</h6>
          <p>SafeTrip is not directed at children under the age of 13. We do not knowingly collect personal information from children.</p>

          <h6 class="section-title">8. Changes to This Policy</h6>
          <p>We may update this Privacy Policy from time to time. We will notify users of significant changes by updating the effective date at the top of this page.</p>

          <h6 class="section-title">9. Contact</h6>
          <p>If you have any questions about this Privacy Policy, please contact us at <a href="mailto:safetripworld.contact@gmail.com">safetripworld.contact@gmail.com</a>.</p>
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
  a { color: #3B82F6; }
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
