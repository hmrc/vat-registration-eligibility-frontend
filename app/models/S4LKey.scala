/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

trait S4LKey[T] {

  val key: String

}

object S4LKey {

  def apply[T](implicit cacheKey: S4LKey[T]): S4LKey[T] = cacheKey

  def apply[T](k: String): S4LKey[T] = new S4LKey[T] {
    override val key = k
  }

}
