package io.github.songminkyu.account.audit;

import io.github.songminkyu.account.entity.RevisionEntity;
import org.hibernate.envers.RevisionListener;

public class AuditingRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        var revEntity = (RevisionEntity) revisionEntity;
        String userName = "ACCOUNTS_MS";
        revEntity.setUsername(userName);
    }
}