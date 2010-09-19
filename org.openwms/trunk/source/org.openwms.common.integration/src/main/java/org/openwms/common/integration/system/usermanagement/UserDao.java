/*
 * openwms.org, the Open Warehouse Management System.
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.common.integration.system.usermanagement;

import org.openwms.common.domain.system.usermanagement.User;
import org.openwms.common.domain.system.usermanagement.UserPassword;
import org.openwms.common.integration.GenericDao;

/**
 * An UserDao - Adds specific functionality concerning {@link User} Entity
 * classes.
 * <p>
 * Adds additional behavior to the {@link GenericDao} to operate on {@link User}
 * Entity classes.
 * </p>
 * 
 * @author <a href="mailto:openwms@googlemail.com">Heiko Scherrer</a>
 * @version $Revision$
 * @since 0.1
 * @see org.openwms.common.integration.GenericDao
 * @see org.openwms.common.domain.system.usermanagement.User
 */
public interface UserDao extends GenericDao<User, Long> {

    /**
     * Search and return an {@link User} with its userName and password, stored
     * in userPassword.
     * 
     * @param userPassword
     *            Stores the userName and password.
     * @return The User when found, otherwise <code>null</code>
     */
    User findByNameAndPassword(UserPassword userPassword);
}