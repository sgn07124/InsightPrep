package com.project.InsightPrep.domain.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.project.InsightPrep.domain.post.dto.PostRequest.Create;
import com.project.InsightPrep.domain.post.dto.PostRequest.PostOwnerStatusDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.post.entity.PostStatus;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.mapper.SharedPostMapper;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.repository.AnswerRepository;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SharedPostServiceImplTest {

    @Mock
    SecurityUtil securityUtil;

    @Mock
    SharedPostMapper sharedPostMapper;

    @Mock
    AnswerMapper answerMapper;

    @Mock
    AnswerRepository answerRepository;

    @InjectMocks
    SharedPostServiceImpl service;

    @Test
    @DisplayName("createPost: 성공")
    void createPost_success() {
        long memberId = 10L;
        long answerId = 111L;

        Create req = Create.builder()
                .title("t")
                .content("c")
                .answerId(answerId)
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(memberId);
        given(answerRepository.existsByIdAndMemberId(answerId, memberId)).willReturn(true);
        given(sharedPostMapper.insertSharedPost(eq("t"), eq("c"), eq(answerId), eq(memberId), eq(PostStatus.OPEN.name())))
                .willReturn(1);
        given(sharedPostMapper.lastInsertedId()).willReturn(999L);

        Long id = service.createPost(req);

        assertThat(id).isEqualTo(999L);
        verify(sharedPostMapper).insertSharedPost("t", "c", answerId, memberId, PostStatus.OPEN.name());
        verify(sharedPostMapper).lastInsertedId();
    }

    @Test
    @DisplayName("createPost: 내 답변이 아니면 FORBIDDEN_OR_NOT_FOUND_ANSWER")
    void createPost_forbiddenOrNotFoundAnswer() {
        long memberId = 10L;
        long answerId = 111L;

        Create req = Create.builder()
                .title("t")
                .content("c")
                .answerId(answerId)
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(memberId);
        given(answerRepository.existsByIdAndMemberId(answerId, memberId)).willReturn(false);

        assertThatThrownBy(() -> service.createPost(req))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.FORBIDDEN_OR_NOT_FOUND_ANSWER.getMessage());

        verify(sharedPostMapper, never()).insertSharedPost(anyString(), anyString(), anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("createPost: insert 실패 시 CREATE_FAILED")
    void createPost_createFailed() {
        long memberId = 10L;
        long answerId = 111L;

        Create req = Create.builder()
                .title("t")
                .content("c")
                .answerId(answerId)
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(memberId);
        given(answerRepository.existsByIdAndMemberId(answerId, memberId)).willReturn(true);
        given(sharedPostMapper.insertSharedPost(anyString(), anyString(), anyLong(), anyLong(), anyString()))
                .willReturn(0);

        assertThatThrownBy(() -> service.createPost(req))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.CREATE_FAILED.getMessage());
    }

    @Test
    @DisplayName("getPostDetail: 성공")
    void getPostDetail_success() {
        long postId = 7L;
        long viewerId = 10L;

        PostDetailDto dto = PostDetailDto.builder()
                .postId(postId)
                .title("t")
                .content("c")
                .status(PostStatus.OPEN.name())
                .createdAt(LocalDateTime.now())
                .authorId(10L)
                .authorNickname("me")
                .questionId(1L)
                .category("CS")
                .question("Q?")
                .answerId(111L)
                .answer("A")
                .feedbackId(null)
                .score(null)
                .improvement(null)
                .modelAnswer(null)
                // 필요 시 서비스에서 채워지는 필드가 있다면 거기에 맞춰 준다
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(viewerId);
        given(sharedPostMapper.findPostDetailById(postId, viewerId)).willReturn(dto);

        PostDetailDto res = service.getPostDetail(postId);

        assertThat(res.getPostId()).isEqualTo(postId);
        verify(sharedPostMapper).findPostDetailById(postId, viewerId);
    }

    @Test
    @DisplayName("getPostDetail: 게시글 없음 → POST_NOT_FOUND")
    void getPostDetail_notFound() {
        long postId = 7L;
        long viewerId = 10L;

        given(securityUtil.getLoginMemberId()).willReturn(viewerId);
        given(sharedPostMapper.findPostDetailById(postId, viewerId)).willReturn(null);

        assertThatThrownBy(() -> service.getPostDetail(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("resolve: 성공")
    void resolve_success() {
        long postId = 5L;
        long loginId = 10L;

        PostOwnerStatusDto row = PostOwnerStatusDto.builder()
                .memberId(loginId)
                .status("OPEN")
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostMapper.findOwnerAndStatus(postId)).willReturn(row);
        given(sharedPostMapper.updateStatusToResolved(postId)).willReturn(1);

        service.resolve(postId);

        verify(sharedPostMapper).updateStatusToResolved(postId);
    }

    @Test
    @DisplayName("resolve: 게시글 없음 → POST_NOT_FOUND")
    void resolve_notFound() {
        long postId = 5L;
        long loginId = 10L;

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostMapper.findOwnerAndStatus(postId)).willReturn(null);

        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("resolve: 본인 글 아님 → FORBIDDEN")
    void resolve_forbidden() {
        long postId = 5L;
        long loginId = 10L;

        PostOwnerStatusDto row = PostOwnerStatusDto.builder()
                .memberId(999L) // 다른 사람
                .status("OPEN")
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostMapper.findOwnerAndStatus(postId)).willReturn(row);

        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("resolve: 이미 RESOLVED → ALREADY_RESOLVED")
    void resolve_alreadyResolved() {
        long postId = 5L;
        long loginId = 10L;

        PostOwnerStatusDto row = PostOwnerStatusDto.builder()
                .memberId(loginId)
                .status("RESOLVED")
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostMapper.findOwnerAndStatus(postId)).willReturn(row);

        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.ALREADY_RESOLVED.getMessage());
    }

    @Test
    @DisplayName("resolve: 업데이트 실패 → CONFLICT")
    void resolve_conflict() {
        long postId = 5L;
        long loginId = 10L;

        PostOwnerStatusDto row = PostOwnerStatusDto.builder()
                .memberId(loginId)
                .status("OPEN")
                .build();

        given(securityUtil.getLoginMemberId()).willReturn(loginId);
        given(sharedPostMapper.findOwnerAndStatus(postId)).willReturn(row);
        given(sharedPostMapper.updateStatusToResolved(postId)).willReturn(0);

        assertThatThrownBy(() -> service.resolve(postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining(PostErrorCode.CONFLICT.getMessage());
    }

    @Test
    @DisplayName("getPosts: 정상 페이징(보정 포함)과 결과 매핑")
    void getPosts_success() {
        // page=0, size=1000 들어와도 보정: page=1, size=50
        int reqPage = 0;
        int reqSize = 1000;
        int safePage = 1;
        int safeSize = 50;
        int offset = 0;

        List<PostListItemDto> rows = List.of(
                PostListItemDto.builder()
                        .postId(1L)
                        .title("T1")
                        .status("OPEN")
                        .createdAt(LocalDateTime.now())
                        .question("Q1")
                        .category("CS")
                        .build()
        );

        given(sharedPostMapper.findSharedPostsPaged(safeSize, offset)).willReturn(rows);
        given(sharedPostMapper.countSharedPosts()).willReturn(1L);

        PageResponse<PostListItemDto> res = service.getPosts(reqPage, reqSize);

        assertThat(res.getPage()).isEqualTo(safePage);
        assertThat(res.getSize()).isEqualTo(safeSize);
        assertThat(res.getTotalElements()).isEqualTo(1L);
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getTitle()).isEqualTo("T1");

        verify(sharedPostMapper).findSharedPostsPaged(safeSize, offset);
        verify(sharedPostMapper).countSharedPosts();
    }
}