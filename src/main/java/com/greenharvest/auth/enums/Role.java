package com.greenharvest.auth.enums;

/**
 * The three roles required by the brief.
 *
 * Endpoint access matrix (documented here so it lives next to the source
 * of truth, and duplicated in README.md for reviewers):
 *
 *   ADMINISTRATOR:
 *     - Full access: user management (create/update roles), products,
 *       suppliers, purchases, sales, inventory dashboard.
 *
 *   WAREHOUSE_OFFICER:
 *     - Products: read, create, update stock via purchases.
 *     - Suppliers: read, create.
 *     - Purchases: full access (this is how stock enters the system).
 *     - Sales: read only.
 *     - Inventory dashboard: read.
 *     - Cannot manage users.
 *
 *   SALES_OFFICER:
 *     - Products: read only (needs to see stock before selling).
 *     - Suppliers: no access.
 *     - Purchases: no access.
 *     - Sales: full access (this is how stock leaves the system).
 *     - Inventory dashboard: read.
 *     - Cannot manage users.
 */
public enum Role {
    ADMINISTRATOR,
    WAREHOUSE_OFFICER,
    SALES_OFFICER
}
