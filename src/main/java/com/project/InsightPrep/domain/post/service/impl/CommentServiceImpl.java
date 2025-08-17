package com.project.InsightPrep.domain.post.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.post.dto.CommentRequest.CreateDto;
import com.project.InsightPrep.domain.post.dto.CommentRequest.UpdateDto;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRow;
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

    @Override
    @Transactional
    public void updateComment(long postId, long commentId, UpdateDto req) {
        SharedPost post = sharedPostMapper.findById(postId);
        if (post == null) throw new PostException(PostErrorCode.POST_NOT_FOUND);

        CommentRow comment = commentMapper.findRowById(commentId);
        if (comment == null) throw new PostException(PostErrorCode.COMMENT_NOT_FOUND);

        // postId 매칭 검증
        if (comment.getPostId() != postId) throw new PostException(PostErrorCode.COMMENT_NOT_FOUND);

        // 본인이 작성한 댓글 검증
        long me = securityUtil.getLoginMemberId();
        if (comment.getMemberId() != me) throw new PostException(PostErrorCode.COMMENT_FORBIDDEN);

        int n = commentMapper.updateContent(commentId, me, req.getContent());
        if (n == 0) {
            throw new PostException(PostErrorCode.COMMENT_FORBIDDEN);
        }
    }

    @Override
    @Transactional
    public void deleteComment(long postId, long commentId) {
        SharedPost post = sharedPostMapper.findById(postId);
        if (post == null) throw new PostException(PostErrorCode.POST_NOT_FOUND);

        CommentRow comment = commentMapper.findRowById(commentId);
        if (comment == null) throw new PostException(PostErrorCode.COMMENT_NOT_FOUND);
        if (comment.getPostId() != postId) throw new PostException(PostErrorCode.COMMENT_NOT_FOUND);

        long me = securityUtil.getLoginMemberId();
        int n = commentMapper.deleteByIdAndMember(commentId, me);
        if (comment.getMemberId() != me) throw new PostException(PostErrorCode.COMMENT_FORBIDDEN);
        if (n == 0) {
            throw new PostException(PostErrorCode.COMMENT_FORBIDDEN);
        }
    }
}
