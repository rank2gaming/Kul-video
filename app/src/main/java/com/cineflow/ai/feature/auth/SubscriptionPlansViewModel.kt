package com.cineflow.ai.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ROUTE_SUBSCRIPTION_PLANS = "subscription_plans"

enum class SubscriptionBillingCycle(val label: String) {
  MONTHLY("Monthly"),
  YEARLY("Yearly")
}

enum class SubscriptionPlanTier(val displayName: String) {
  FREE("Free Plan"),
  PRO("Pro Plan")
}

enum class SubscriptionDialogType(
  val title: String,
  val body: String
) {
  UPGRADE_TO_PRO(
    title = "Pro Plan Upgrade (Prepared)",
    body = "CineFlow AI Pro Plan UI and SubscriptionServiceContract are prepared. Per Section 21 policy, payment processing is currently disabled and no payment orders, billing charges, or simulated paid entitlements are executed."
  ),
  MANAGE_SUBSCRIPTION(
    title = "Manage Subscription",
    body = "Current Plan: Free Plan (₹0 / Forever)\nStorage Quota: 1 GB Cloud Storage (Up to 5 Projects)\nRenewal Status: Active — Forever Free (Payment integration disabled per Section 21)."
  ),
  SUBSCRIPTION_HISTORY(
    title = "Subscription History",
    body = "No past payment transactions or billing invoices exist for this account. Payment integration is currently disabled per Section 21."
  )
}

/**
 * Prepared contract interface for future authorized subscription & payment integration (Section 21).
 * Payment integration remains strictly disabled during current development.
 */
interface SubscriptionServiceContract {
  val isPaymentIntegrationEnabled: Boolean
    get() = false

  fun currentEntitledTier(): SubscriptionPlanTier = SubscriptionPlanTier.FREE
}

/**
 * ViewModel for the Subscription / Plans Screen (`CineFlow AI Neon Subscription Plans.png` & Section 21).
 */
class SubscriptionPlansViewModel : ViewModel(), SubscriptionServiceContract {

  private val _selectedBillingCycle = MutableStateFlow(SubscriptionBillingCycle.MONTHLY)
  val selectedBillingCycle: StateFlow<SubscriptionBillingCycle> = _selectedBillingCycle.asStateFlow()

  private val _currentPlanTier = MutableStateFlow(SubscriptionPlanTier.FREE)
  val currentPlanTier: StateFlow<SubscriptionPlanTier> = _currentPlanTier.asStateFlow()

  private val _activeDialog = MutableStateFlow<SubscriptionDialogType?>(null)
  val activeDialog: StateFlow<SubscriptionDialogType?> = _activeDialog.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  fun selectBillingCycle(cycle: SubscriptionBillingCycle) {
    _selectedBillingCycle.value = cycle
  }

  /**
   * Section 21: Payment integration is currently DISABLED.
   * Never create payment orders, send payment requests, simulate payment success, or grant paid entitlements.
   */
  fun requestUpgradeToPro() {
    _activeDialog.value = SubscriptionDialogType.UPGRADE_TO_PRO
    _statusMessage.value = SubscriptionDialogType.UPGRADE_TO_PRO.body
  }

  fun openManageSubscription() {
    _activeDialog.value = SubscriptionDialogType.MANAGE_SUBSCRIPTION
    _statusMessage.value = SubscriptionDialogType.MANAGE_SUBSCRIPTION.body
  }

  fun openSubscriptionHistory() {
    _activeDialog.value = SubscriptionDialogType.SUBSCRIPTION_HISTORY
    _statusMessage.value = SubscriptionDialogType.SUBSCRIPTION_HISTORY.body
  }

  fun dismissDialog() {
    _activeDialog.value = null
  }
}
