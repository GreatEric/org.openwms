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
package org.openwms.tms.service.order.delegate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openwms.common.domain.TransportUnit;
import org.openwms.tms.domain.order.TransportOrder;
import org.openwms.tms.domain.values.TransportOrderState;
import org.openwms.tms.integration.TransportOrderDao;
import org.openwms.tms.service.impl.TransportOrderStateDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A DefaultOrderStateDelegate.
 * 
 * @author <a href="mailto:russelltina@users.sourceforge.net">Tina Russell</a>
 * @version $Revision: $
 * @since 0.1
 */
@Service
public class DefaultOrderStateDelegate implements TransportOrderStateDelegate {

    protected TransportOrderDao dao;

    public DefaultOrderStateDelegate(TransportOrderDao dao) {
        this.dao = dao;
    }

    /**
     * Logger instance can be used by subclasses.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @see org.openwms.tms.service.impl.TransportOrderStateDelegate#afterCreation(java.lang.Long)
     */
    @Override
    public void afterCreation(TransportUnit transportUnit) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transportUnit", transportUnit);
        params.put("states", new Enum[] { TransportOrderState.CREATED });
        List<TransportOrder> transportOrders = dao
                .findByNamedParameters(TransportOrder.NQ_FIND_FOR_TU_IN_STATE, params);
        if (logger.isDebugEnabled()) {
            logger.debug("List:" + transportOrders.size());
        }
        for (TransportOrder transportOrder : transportOrders) {
            if (logger.isDebugEnabled()) {
                logger.debug("Initialize TransportOrder :" + transportOrder.getId());
            }
            boolean go = initialize(transportOrder);
            if (go) go = start(transportOrder);
        }
    }

    /**
     * @see org.openwms.tms.service.impl.TransportOrderStateDelegate#afterFinish(java.lang.Long)
     */
    @Override
    public void afterFinish(Long id) {
        TransportOrder orderToStart = findOrderToStart(id);
        if (orderToStart != null) {
            start(orderToStart);
        }
    }

    /**
     * @see org.openwms.tms.service.impl.TransportOrderStateDelegate#onCancel(java.lang.Long)
     */
    @Override
    public void onCancel(Long id) {
        TransportOrder orderToStart = findOrderToStart(id);
        if (orderToStart != null) {
            start(orderToStart);
        } else {
            logger.warn("TransportOrder with ID not found to do processing after cancel:" + id);
        }
    }

    /**
     * @see org.openwms.tms.service.impl.TransportOrderStateDelegate#onFailure(java.lang.Long)
     */
    @Override
    public void onFailure(Long id) {
        TransportOrder orderToStart = findOrderToStart(id);
        if (orderToStart != null) {
            start(orderToStart);
        }
    }

    /**
     * @see org.openwms.tms.service.impl.TransportOrderStateDelegate#onInterrupt(java.lang.Long)
     */
    @Override
    public void onInterrupt(Long id) {}

    // TODO [russelltina] : Better to find and retrieve the best one to always
    // fetch the whole list
    @SuppressWarnings("unchecked")
    private TransportOrder findOrderToStart(Long id) {
        TransportOrder transportOrder = dao.findById(id);
        if (transportOrder == null) {
            logger.warn("No TransportOrder found with id:" + id);
            return null;
        }
        logger.debug("Serach an order to start");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transportUnit", transportOrder.getTransportUnit());
        List<TransportOrder> transportOrders = dao
                .findByNamedParameters(TransportOrder.NQ_FIND_ORDERS_TO_START, params);
        if (transportOrders != null) {
            return transportOrders.get(0);
        }
        logger.debug("No order found that could be started");
        return null;
    }

    private boolean initialize(TransportOrder transportOrder) {
        transportOrder.setState(TransportOrderState.INITIALIZED);
        return true;
    }

    private boolean start(TransportOrder transportOrder) {
        if (transportOrder.getTargetLocationGroup() != null
                && transportOrder.getTargetLocationGroup().isInfeedBlocked()) {
            if (logger.isDebugEnabled()) {
                logger.debug("TargetLocationGroup is blocked, do not start");
            }
            return false;
        }

        // Check for other active transports
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transportUnit", transportOrder.getTransportUnit());
        params.put("states", new Object[] { TransportOrderState.STARTED, TransportOrderState.INTERRUPTED });
        List<TransportOrder> others = dao.findByNamedParameters(TransportOrder.NQ_FIND_FOR_TU_IN_STATE, params);
        if (others == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("There is an already started one");
            }
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("No active transportOrder found for transportUnit : " + transportOrder.getTransportUnit());
        }

        transportOrder.setState(TransportOrderState.STARTED);
        return true;
    }
}
