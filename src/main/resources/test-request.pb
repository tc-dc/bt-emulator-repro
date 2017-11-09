table_name: "projects/repro/instances/repro/tables/itd_a6f34e35cd60454c84243e5eae84d79e"
rows {
  row_ranges {
    start_key_closed: "\000\000\000\000\000\000\000\002\000\000\000\000\001_P\324 \000"
    end_key_closed: "\000\000\000\000\000\000\000\002\000\000\000\000\001_U\372|\000"
  }
}
filter {
  chain {
    filters {
      chain {
        filters {
          condition {
            predicate_filter {
              chain {
                filters {
                  chain {
                    filters {
                      column_range_filter {
                        family_name: "d"
                        start_qualifier_closed: "\000\000"
                        end_qualifier_closed: "\000\000"
                      }
                    }
                    filters {
                      cells_per_column_limit_filter: 1
                    }
                  }
                }
                filters {
                  value_range_filter {
                    start_value_closed: "\002"
                    end_value_closed: "\002"
                  }
                }
              }
            }
            true_filter {
              cells_per_column_limit_filter: 2147483647
            }
          }
        }
        filters {
          condition {
            predicate_filter {
              chain {
                filters {
                  chain {
                    filters {
                      column_range_filter {
                        family_name: "d"
                        start_qualifier_closed: "\000\001"
                        end_qualifier_closed: "\000\001"
                      }
                    }
                    filters {
                      cells_per_column_limit_filter: 1
                    }
                  }
                }
                filters {
                  interleave {
                    filters {
                      value_range_filter {
                        start_value_closed: "\001"
                        end_value_closed: "\001"
                      }
                    }
                    filters {
                      value_range_filter {
                        start_value_closed: "\002"
                        end_value_closed: "\002"
                      }
                    }
                  }
                }
              }
            }
            true_filter {
              cells_per_column_limit_filter: 2147483647
            }
          }
        }
      }
    }
    filters {
      interleave {
        filters {
          chain {
            filters {
              family_name_regex_filter: "c"
            }
            filters {
              column_qualifier_regex_filter: "..0a"
            }
          }
        }
        filters {
          chain {
            filters {
              family_name_regex_filter: "s"
            }
            filters {
              column_qualifier_regex_filter: "..0b"
            }
          }
        }
        filters {
          chain {
            filters {
              family_name_regex_filter: "d"
            }
            filters {
              column_range_filter {
                family_name: "d"
                start_qualifier_closed: "\000\001"
                end_qualifier_closed: "\000\001"
              }
            }
          }
        }
      }
    }
  }
}
