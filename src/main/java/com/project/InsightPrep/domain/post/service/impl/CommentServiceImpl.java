package com.project.InsightPrep.domain.post.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.post.dto.CommentRequest.CreateDto;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.entity.Comment;
import com.project.InsightPrep.domain.post.entity.SharedPost;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.mapper.CommentMapper;
import com.project.InsightPrep.domain.post.mapper.SharedPostMapper;
import com.project.InsightPrep.domain.post.service.CommentService;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final SecurityUtil securityUtil;
    private final SharedPostMapper sharedPostMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentRes createComment(long postId, CreateDto req) {
        Member me = securityUtil.getAuthenticatedMember();

        SharedPost post = sharedPostMapper.findById(postId);
        if (post == null) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }

        Comment comment = Comment.builder()
                .content(req.getContent())
                .member(me)
                .sharedPost(post)
                .build();

        commentMapper.insertComment(comment);

        return CommentRes.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorId(me.getId())
                .authorNickname(me.getNickname())
                .postId(postId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
