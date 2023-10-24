package org.tybloco.ui.main;

/*-
 * #%L
 * ui
 * %%
 * Copyright (C) 2023 Montoni
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.springframework.context.annotation.*;
import org.tybloco.ui.lib.repo.ArtifactResolver;

@ComponentScan(lazyInit = true)
@Configuration(proxyBeanMethods = false)
@PropertySource(name = "ui", value = "classpath:tybloco/ui.properties", encoding = "UTF-8")
@PropertySource(name = "schemas", value = "classpath:tybloco/schemas.properties", encoding = "UTF-8")
public class MainConfiguration {

  @Bean
  public ArtifactResolver artifactResolver() {
    return new ArtifactResolver();
  }
}
