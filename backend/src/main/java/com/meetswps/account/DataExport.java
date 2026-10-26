package com.meetswps.account;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Everything stored about a student, as one JSON document, for the GDPR right of access (art. 15) and data
 * portability (art. 20). Leaves out the password hash, likes received and reports about the student, since those
 * would reveal what other students did.
 */
@Component
class DataExport {

    private static final String SQL = """
            select json_build_object(
              'exportedAt', now(),
              'account', (select row_to_json(u) from (
                  select id, email, first_name, role, email_verified, privacy_policy_version,
                         privacy_policy_accepted_at, created_at
                  from users where id = :id) u),
              'profile', (select row_to_json(p) from profiles p where p.user_id = :id),
              'photos', coalesce((select json_agg(x order by x.position) from (
                  select id, position, content_type, created_at from photos where user_id = :id) x), '[]'),
              'likesSent', coalesce((select json_agg(x order by x.created_at) from (
                  select l.to_id as student_id, u.first_name as student_first_name, l.intent, l.created_at
                  from likes l join users u on u.id = l.to_id where l.from_id = :id) x), '[]'),
              'matches', coalesce((select json_agg(x order by x.created_at) from (
                  select m.id, u.id as student_id, u.first_name as student_first_name, m.intent, m.created_at
                  from matches m
                  join users u on u.id = case when m.user_low = :id then m.user_high else m.user_low end
                  where :id in (m.user_low, m.user_high)) x), '[]'),
              'messages', coalesce((select json_agg(x order by x.created_at) from (
                  select msg.match_id, msg.sender_id = :id as sent_by_me, msg.body, msg.created_at
                  from messages msg join matches m on m.id = msg.match_id
                  where :id in (m.user_low, m.user_high)) x), '[]'),
              'blocks', coalesce((select json_agg(x order by x.created_at) from (
                  select b.blocked_id as student_id, u.first_name as student_first_name, b.created_at
                  from blocks b join users u on u.id = b.blocked_id where b.blocker_id = :id) x), '[]'),
              'reportsMade', coalesce((select json_agg(x order by x.created_at) from (
                  select r.target_id as student_id, u.first_name as student_first_name, r.reason, r.details,
                         r.created_at, r.resolved_at
                  from reports r join users u on u.id = r.target_id where r.reporter_id = :id) x), '[]'),
              'pushTokens', coalesce((select json_agg(x order by x.updated_at) from (
                  select token, updated_at from push_tokens where user_id = :id) x), '[]')
            )::text
            """;

    private final JdbcClient jdbc;

    DataExport(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    String json(UUID userId) {
        return jdbc.sql(SQL).param("id", userId).query(String.class).single();
    }
}
