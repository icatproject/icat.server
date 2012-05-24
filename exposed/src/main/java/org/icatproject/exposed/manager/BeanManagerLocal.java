package org.icatproject.exposed.manager;

import java.util.List;

import javax.ejb.Local;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.manager.EntityInfo;

/**
 * This is the business interface for Manager enterprise bean.
 */
@Local
public interface BeanManagerLocal {

	long create(String sessionId, EntityBaseBean bean) throws IcatException;

	void delete(String sessionId, EntityBaseBean bean) throws IcatException;

	void update(String sessionId, EntityBaseBean bean) throws IcatException;

	EntityBaseBean get(String sessionId, String query, long primaryKey) throws IcatException;

	List<?> search(String sessionId, String query) throws IcatException;

	void dummy(Facility facility);

	EntityInfo getEntityInfo(String beanName) throws IcatException;

	List<Long> createMany(String sessionId, List<EntityBaseBean> beans) throws IcatException;

}
