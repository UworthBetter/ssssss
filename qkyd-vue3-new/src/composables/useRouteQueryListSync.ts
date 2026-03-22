import { onMounted, watch, type Ref } from 'vue'
import type { LocationQuery, RouteLocationNormalizedLoaded } from 'vue-router'

interface ResolveMatchedItemArgs<TItem> {
  list: TItem[]
  routeQuery: LocationQuery
  fallbackSelected: TItem | null
}

interface UseRouteQueryListSyncOptions<TItem> {
  route: RouteLocationNormalizedLoaded
  list: Ref<TItem[]>
  selected: Ref<TItem | null>
  applyQuery: (routeQuery: LocationQuery) => void
  resolveMatchedItem: (args: ResolveMatchedItemArgs<TItem>) => TItem | null | undefined
  fetchList: () => Promise<void>
}

export const useRouteQueryListSync = <TItem>(options: UseRouteQueryListSyncOptions<TItem>) => {
  const syncSelectedAfterFetch = () => {
    const fallbackSelected = options.selected.value
    const matchedItem = options.resolveMatchedItem({
      list: options.list.value,
      routeQuery: options.route.query,
      fallbackSelected
    })

    if (matchedItem) {
      options.selected.value = matchedItem
      return
    }

    if (fallbackSelected && options.list.value.includes(fallbackSelected)) {
      options.selected.value = fallbackSelected
      return
    }

    options.selected.value = options.list.value[0] || null
  }

  const syncFromRoute = async () => {
    options.applyQuery(options.route.query)
    await options.fetchList()
  }

  const install = () => {
    watch(
      () => options.route.query,
      async () => {
        await syncFromRoute()
      },
      { deep: true }
    )

    onMounted(async () => {
      await syncFromRoute()
    })
  }

  return {
    install,
    syncFromRoute,
    syncSelectedAfterFetch
  }
}
