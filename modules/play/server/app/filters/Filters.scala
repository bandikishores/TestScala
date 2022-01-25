package filters

import akka.stream.Materializer
import play.api.http.{DefaultHttpFilters, EnabledFilters}
import play.filters.gzip.GzipFilter

import javax.inject.Inject

/**
 * Add the following filters by default to all projects
 *
 * https://www.playframework.com/documentation/latest/ScalaCsrf
 * https://www.playframework.com/documentation/latest/AllowedHostsFilter
 * https://www.playframework.com/documentation/latest/SecurityHeaders
 */
class Filters @Inject() ()(implicit mat: Materializer, defaultFilters: EnabledFilters,
                                                 gzip: GzipFilter,
                                                 log: LoggingFilter) extends DefaultHttpFilters (/*defaultFilters.filters :+ gzip :+ */ log) {
  // override def filters: Seq[EssentialFilter] = List()

}
