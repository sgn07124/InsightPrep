package com.project.InsightPrep.domain.post.service.impl;

import com.project.InsightPrep.domain.post.dto.PostRequest.Create;
import com.project.InsightPrep.domain.post.dto.PostRequest.PostOwnerStatusDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.post.entity.PostStatus;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.mapper.SharedPostMapper;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SharedPostServiceImpl implements SharedPostService {

    private final SecurityUtil securityUtil;
    private final SharedPostMapper sharedPostMapper;
    private final AnswerMapper answerMapper;

    @Override
    @Transactional
    public Long createPost(Create req) {
        long memberId = securityUtil.getLoginMemberId();

        boolean myAnswer = answerMapper.existsMyAnswer(req.getAnswerId(), memberId);
        if (!myAnswer) {
            throw new PostException(PostErrorCode.FORBIDDEN_OR_NOT_FOUND_ANSWER);
        }

        int n = sharedPostMapper.insertSharedPost(req.getTitle(), req.getContent(), req.getAnswerId(), memberId, PostStatus.OPEN.name());
        if (n != 1) {
            throw new PostException(PostErrorCode.CREATE_FAILED);
        }

        return sharedPostMapper.lastInsertedId();
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(long postId) {
        PostDetailDto dto = sharedPostMapper.findPostDetailById(postId);
        if (dto == null) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }
        return dto;
    }

    @Override
    @Transactional
    public void resolve(long postId) {
        long loginId = securityUtil.getLoginMemberId();

        PostOwnerStatusDto row = sharedPostMapper.findOwnerAndStatus(postId);
        if (row == null) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }
        if (!row.getMemberId().equals(loginId)) {
            throw new PostException(PostErrorCode.FORBIDDEN);
        }
        if ("RESOLVED".equals(row.getStatus())) {
            throw new PostException(PostErrorCode.ALREADY_RESOLVED);
        }

        int updated = sharedPostMapper.updateStatusToResolved(postId);
        if (updated != 1) {
            throw new PostException(PostErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostListItemDto> getPosts(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;

        List<PostListItemDto> content = sharedPostMapper.findSharedPostsPaged(safeSize, offset);
        long total = sharedPostMapper.countSharedPosts();

        return PageResponse.of(content, safePage, safeSize, total);
    }
}
