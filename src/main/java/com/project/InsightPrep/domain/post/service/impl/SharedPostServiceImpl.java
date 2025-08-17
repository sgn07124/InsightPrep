package com.project.InsightPrep.domain.post.service.impl;

import com.project.InsightPrep.domain.post.dto.PostRequest.Create;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.entity.PostStatus;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.mapper.SharedPostMapper;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
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
}
