package com.project.InsightPrep.domain.post.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.post.dto.CommentRequest.CreateDto;
import com.project.InsightPrep.domain.post.dto.CommentRequest.UpdateDto;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentListItem;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.entity.Comment;
import com.project.InsightPrep.domain.post.entity.SharedPost;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.reqository.CommentRepository;
import com.project.InsightPrep.domain.post.reqository.SharedPostRepository;
import com.project.InsightPrep.domain.post.service.CommentService;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final SecurityUtil securityUtil;
    private final SharedPostRepository sharedPostRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentRes createComment(long postId, CreateDto req) {
        Member me = securityUtil.getAuthenticatedMember();

        SharedPost post = sharedPostRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(req.getContent())
                .member(me)
                .sharedPost(post)
                .build();

        commentRepository.save(comment);

        return CommentRes.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorId(me.getId())
                .authorNickname(me.getNickname())
                .postId(postId)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateComment(long postId, long commentId, UpdateDto req) {
        SharedPost post = sharedPostRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new PostException(PostErrorCode.COMMENT_NOT_FOUND));

        // postId 매칭 검증
        if (!comment.getSharedPost().getId().equals(postId)) throw new PostException(PostErrorCode.COMMENT_NOT_FOUND);

        // 본인이 작성한 댓글 검증
        long me = securityUtil.getLoginMemberId();
        if (!comment.getMember().getId().equals(me)) throw new PostException(PostErrorCode.COMMENT_FORBIDDEN);

        comment.updateContent(req.getContent());
    }

    @Override
    @Transactional
    public void deleteComment(long postId, long commentId) {
        SharedPost post = sharedPostRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new PostException(PostErrorCode.COMMENT_NOT_FOUND));

        // postId 매칭 검증
        if (!comment.getSharedPost().getId().equals(postId)) throw new PostException(PostErrorCode.COMMENT_NOT_FOUND);

        // 본인 작성자 검증
        long me = securityUtil.getLoginMemberId();
        if (!comment.getMember().getId().equals(me)) throw new PostException(PostErrorCode.COMMENT_FORBIDDEN);

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentListItem> getComments(long postId, int page, int size) {
        SharedPost post = sharedPostRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        int safePage = Math.max(page, 1) - 1; // Pageable은 0-based
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));

        Page<Comment> pageResult = commentRepository.findBySharedPost_IdOrderByCreatedAtAscIdAsc(postId, pageable);
        long total = commentRepository.countBySharedPost_Id(postId);

        // 현재 로그인한 사용자 id
        long me = securityUtil.getLoginMemberId();

        List<CommentListItem> content = pageResult.getContent().stream()
                .map(c -> CommentListItem.builder()
                        .commentId(c.getId())
                        .authorId(c.getMember().getId())
                        .authorNickname(c.getMember().getNickname())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .mine(c.getMember().getId().equals(me))
                        .build())
                .toList();

        return PageResponse.of(content, safePage + 1, safeSize, total);
    }
}
