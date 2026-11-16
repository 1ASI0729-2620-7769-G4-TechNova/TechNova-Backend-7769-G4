package com.technova.washtrack.orders.domain.model.valueobjects;

/**
 * Types of garments that can be registered as items of an order.
 *
 */
public enum GarmentType {
    /** Shirts and blouses. */
    SHIRT,
    /** Pants and trousers. */
    PANTS,
    /** Suits. */
    SUIT,
    /** Sheets, blankets and other bedding. */
    BEDDING,
    /** Delicate garments that require special care. */
    DELICATE,
    /** Mixed laundry registered as a single bulk item. */
    BULK_LAUNDRY
}