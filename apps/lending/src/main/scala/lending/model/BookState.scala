package es.eriktorr.library
package lending.model

enum BookState:
  case Available extends BookState
  case CheckedOut extends BookState
  case OnHold extends BookState
