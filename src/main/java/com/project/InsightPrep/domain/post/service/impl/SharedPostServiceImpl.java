package com.project.InsightPrep.domain.post.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.post.dto.PostRequest.Create;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.post.entity.PostStatus;
import com.project.InsightPrep.domain.post.entity.SharedPost;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.reqository.CommentRepository;
import com.project.InsightPrep.domain.post.reqository.SharedPostRepository;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.repository.AnswerRepository;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SharedPostServiceImpl implements SharedPostService {

    private final SecurityUtil securityUtil;
    private final SharedPostRepository sharedPostRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Long createPost(Create req) {
        Member member = securityUtil.getAuthenticatedMember();
        long memberId = member.getId();

        boolean myAnswer = answerRepository.existsByIdAndMemberId(req.getAnswerId(), memberId);
        if (!myAnswer) {
            throw new PostException(PostErrorCode.FORBIDDEN_OR_NOT_FOUND_ANSWER);
        }

        Answer answer = answerRepository.findById(req.getAnswerId()).orElseThrow(() -> new PostException(QuestionErrorCode.ANSWER_NOT_FOUND));

        SharedPost sharedPost = SharedPost.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .answer(answer)
                .member(member)
                .status(PostStatus.OPEN)
                .build();

        return sharedPostRepository.save(sharedPost).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(long postId) {
        long viewerId = securityUtil.getLoginMemberId();
        SharedPost post = sharedPostRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Answer answer = post.getAnswer();
        Question question = answer.getQuestion();
        AnswerFeedback feedback = answer.getFeedback();
        Member author = post.getMember();

        long commentCount = commentRepository.countBySharedPostId(postId);
        return PostDetailDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .authorId(author.getId())
                .authorNickname(author.getNickname())
                .questionId(question.getId())
                .category(question.getCategory())
                .question(question.getContent())
                .answerId(answer.getId())
                .answer(answer.getContent())
                .feedbackId(feedback != null ? feedback.getId() : null)
                .score(feedback != null ? feedback.getScore() : null)
                .improvement(feedback != null ? feedback.getImprovement() : null)
                .modelAnswer(feedback != null ? feedback.getModelAnswer() : null)
                .myPost(author.getId().equals(viewerId))
                .commentCount(commentCount)
                .build();
    }

    @Override
    @Transactional
    public void resolve(long postId) {
        long loginId = securityUtil.getLoginMemberId();

        SharedPost post = sharedPostRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Long ownerId = post.getMember().getId();
        PostStatus status = post.getStatus();

        if (!ownerId.equals(loginId)) {
            throw new PostException(PostErrorCode.FORBIDDEN);
        }
        if (status == PostStatus.RESOLVED) {
            throw new PostException(PostErrorCode.ALREADY_RESOLVED);
        }

        post.markResolved();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostListItemDto> getPosts(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);

        // PageRequest 객체를 통해 limit, offset, 정렬까지 모두 처리됨
        PageRequest pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        // JPA가 자동으로 페이징 쿼리 실행 + count 쿼리도 자동 수행
        Page<SharedPost> postsPage = sharedPostRepository.findAll(pageable);

        List<PostListItemDto> content = postsPage.getContent().stream()
                .map(sp -> PostListItemDto.builder()
                        .postId(sp.getId())
                        .title(sp.getTitle())
                        .createdAt(sp.getCreatedAt())
                        .status(sp.getStatus().name())
                        .question(sp.getAnswer().getQuestion().getContent())
                        .category(sp.getAnswer().getQuestion().getCategory())
                        .build())
                .toList();

        long total = postsPage.getTotalElements();

        return PageResponse.of(content, safePage, safeSize, total);
    }
}
